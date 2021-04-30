# Spring源码阅读笔记

### 问：Spring容器的生命周期分为几个阶段？
### 答：<br>
AbstractApplicationContext>refresh() <br>

#### 1.Spring容器刷新上下文准备阶段 <br>
#### AbstractApplicationContext>prepareRefresh(); <br>

a. 启动时间 <br>

b. 容器状态标识 <br>


c. 初始化PropertySources<br>
**initPropertySources(),这是个空方法，需要子类去扩展这个方法** 

d. 检验Environment中的必要属性 <br>

e. 初始化事件监听集合 <br>

f. 初始化早期Spring事件集合 <br>

#### 2.**BeanFactory创建阶段（核心方法）**<br>
#### AbstractApplicationContext>obtainFreshBeanFactory(); <br>

a.刷新 Spring容器上下文底层 <br>

 i. 销毁或关闭BeanFactory, 如果已经存在的话 <br>

 ii. 创建BeanFactory:createBeanFactory() <br>

 iii. 设置或更新BeanFactory Id, 方便序列化和反序列化 <br>

	序列化是指:    Object转json
	反序列化是指:  json转Object
	
	问：为什么需要序列化与反序列化？

	答：我们知道，当两个进程进行远程通信时，可以相互发送各种类型的数据，包括文本、图片、音频、视频等，
	而这些数据都会以二进制序列的形式在网络上传送。
	那么当两个Java进程进行通信时，能否实现进程间的对象传送呢？
	答案是可以的。如何做到呢？这就需要Java序列化与反序列化了。
	换句话说，一方面，发送方需要把这个Java对象转换为字节序列，然后在网络上传送；
	另一方面，接收方需要从字节序列中恢复出Java对象。
	
	序列化ID起着关键的作用，它决定着是否能够成功反序列化
	Java的序列化机制是通过判断运行时类的SerialVersionUID来验证版本一致性的，
	在进行反序列化时，JVM会把传进来的字节流中的serialVersionUID与本地实体类中的serialVersionUID进行比较。
	如果相同则认为是一致的，便可以进行反序列化，否则就会报序列化版本不一致的异常。

 iv. 设置"是否允许BeanDefinition重复定义"；设置"是否允许循环依赖" <br>
 
 v. **核心步骤**：加载BeanDefinition：loadBeanDefinitions(DefaultListableBeanFactory); <br>
 
 **这一步实际上就从XML配置文件里把bean信息给读取到了Factory里了。<br>
   但这里是存储成ConcurrentHashMap数据结构，<br>
   后面的第10步"10.BeanFactory初始化完成阶段：finishBeanFactoryInitialization(beanFactory)"<br>
   才会把ConcurrentHashMap转换成POJO实例** <br>
 
 AbstractXmlApplicationContext.loadBeanDefinitions(beanDefinitionReader)>...><br>
 
 reader.loadBeanDefinitions(configLocations)>...><br>
 
 XmlBeanDefinitionReader.loadBeanDefinitions(resource)>...><br>
 
 doLoadBeanDefinitions(inputSource, encodedResource.getResource())><br>
 
 registerBeanDefinitions(doc, resource)><br>
 
 registerBeanDefinitions(doc, createReaderContext(resource))>...><br>
 
 parseBeanDefinitions(root, this.delegate)><br>
 
 parseDefaultElement(ele, delegate)>processBeanDefinition(ele, delegate)><br>
 
 delegate.parseBeanDefinitionElement(ele)>...><br>
 
 parseBeanDefinitionElement(ele, beanName, containingBean)><br>
 
     //通过构造器解析参数值
     //本程序xml中id="user"的节点就是通过constructor-arg设置bean的值的，最终被这一方法所解析出来。
     parseConstructorArgElements(ele, bd);
     
     
     //通过Setter解析值
     //本程序xml中id="person"的节点就是通过property属性设置bean的值的，最终被这一方法所解析出来。
     parsePropertyElements(ele, bd)
     
     //还有种通过qualifier属性注入bean的方式，暂且不表
     parseQualifierElements(ele, bd)
     
     问：构造器注入 vs. Setter注入的区别 ？
  
     答：1.构造函数的参数顺序和Stter注入的参数顺序都保证了对象的初始化顺序
     （构造器存储注入值的数据结构是LinkedHashMap，
      Setter存储注入值的数据结构是ArrayList）
      就性能而言LinkedHashMap，get(), put(), remove(). conyainsKey()一切都是O(1)时间复杂度，并且保留插入顺序。
      如果使用ArrayList add(存)、get(取)它将是O(1)，而查找，插入和删除他的复杂度是O(N)
  
      2. Setter有违面向对象原理中封装特性，把面向对象编程风格退化成了面向过程编程风格，
         构造器注入保证的对象的不变性，属性或者类都可用final修饰
  
      3. Setter注入可能会导致NPE（空指针异常）的存在，但构造器注入有个劣势是参数过多时，不够美观
      
     问：读取xml中的多个bean节点后，存储的数据结构是ConcurrentHashMap，此数据结构的优势是？
  
     答：ConcurrentHashMap一般配合synchronized使用，比如：synchronized(this.singletonObjects){...};
     ConcurrentHashMap的实现使用了一个包含16个锁的数组，每一个锁都守护HashMap的1/16。
     假设Hash值均匀分布，这将会把对于锁的请求减少到约为原来的1/16。
     这项技术使得ConcurrentHashMap能够支持16个并发。
     当多处理器系统的大负荷访问需要更好的并发性时，锁的数量还可以增加。
 
 
 
 vi. 关联新建BeanFactory到Spring容器上下文中 <br>

b.返回Spring容器上下文底层getBeanFactory() <br>

#### 3.BeanFactory初始化阶段 <br>
#### AbstractApplicationContext>prepareBeanFactory(); <br>

a. 关联classloader <br>

b. 设置Bean表达式处理器(Spring EL #{...}; @Value) <br>
[Spring EL](https://blog.csdn.net/keda8997110/article/details/52767087) <br>

c. 添加PropertyEditorRegistrar实现： ResourceEditorRegistrar <br>

d. 添加Aware回调接口BeanPostProcessor实现：ApplicationContextAwareProcessor <br>

e. 添加Aware回调接口作为依赖注入接口 <br>

f. 注册ResolvableDependency内建的非Bean对象：BeanFactory、ResourceLoader、<br>
ApplicationEventPublisher以及ApplicationContext <br>

g. 注册ApplicationListenerDetector对象 <br>
	 
	 对于符合ApplicationListener接口且是单例的Bean记录到singletonsName,
	 后面会作为ApplicaitonListener关联到ApplicationContext中

h. 注册LoadTimeWeaverAwareProcessor对象（AOP相关）<br>

i. 注册单例对象：Environment、 Java System Properties以及OS环境变量 <br>

#### 4.BeanFactory后置处理阶段 <br>

a. AbstractApplicationContext>postProcessBeanFactory(beanFactory); <br>
**这是个空方法，需要子类去扩展这个方法** <br>

b. AbstractApplicationContext>invokeBeanFactoryPostProcessors(beanFactory); <br>

 i. 迭代所有BeanFactoryPostProcessor判断它是否是BeanDefinitionRegistryPostProcessor类型 <br>
	
	1. 如果是则执行其postProcessBeanDefinitionRegistry方法，
	并在registryProcessors集合中加入当前迭代对象BeanFactoryPostProcessor。

	2. 否则直接加入regularPostProcessors集合中，表示是常规的PostProcessor。

 ii. Spring容器中的BeanFactoryPostProcessor有可能实现了PriorityOrdered、Ordered接口，也可能没有实现。<br>

 对他们进行分别归类之后按照PriorityOrdered、Ordered、常规的BeanFactoryPostProcessor的集合顺序去执行。<br>

 iii. 分别遍历处理registryProcessors和regularPostProcessors两个集合对象，<br>

 执行迭代对象的postProcessBeanFactory方法，因为其实集合的对象都实现了BeanFactoryPostProcessor接口。<br>

#### 5.BeanFactory注册BeanPostProcessor阶段 <br>
// 针对Bean的扩展 <br>
#### AbstractApplicationContext>registerBeanPostProcessors(beanFactory)； <br>

a. 注册PriorityOrdered类型的 BeanPostProcessor Beans <br>

b. 注册Ordered类型的  BeanPostProcessor Beans <br>

c. 注册普通类型的（nonOrdered）BeanPostProcessor Beans <br>

d. 注册MergedBeanDefinitionPostProcessor Beans <br>

e. 注册ApplicationListenerDetector对象 <br>


#### 6.国际化初始化阶段 <br>
#### AbstractApplicationContext>initMessageSource(); <br>

a.在Spring IOC容器中依赖查找MessageSource并根据情况设置其层次性 <br>

b.如果Spring IOC容器中没有则创建一个DelegatingMessageSource并设置其层次性，<br>

然后在Spring IOC容器中注册MessageSource单例对象 <br>

#### 7.应用事件广播器初始化阶段 <br>
#### AbstractApplicationContext>initApplicationEventMulticaster(); <br>

a. 在Spring容器中判断并依赖查找ApplicationEventMulticaster对象，<br>

并赋值给当前容器的applicationEventMulticaster <br>

b. 如果IOC容器中没有则创建一个SimpleApplicationEventMulticaster，<br>

然后在Spring容器中注册ApplicationEventMulticaster单例对象。<br>

#### 8.Spring应用上下文刷新阶段 <br>
#### //初始化特殊Bean <br>
#### AbstractApplicationContext>onRefresh(); <br>
**这是个空方法，需要子类去扩展这个方法** <br>

#### 9.Spring注册监听器阶段 <br>
#### AbstractApplicationContext>registerListeners(); <br>

a. 添加当前应用上下文所关联的ApplicationListener集合对象 <br>

b. 添加BeanFactory注册的ApplicationListener Beans <br>

c. 广播早期的Spring事件 <br>

#### 10.**BeanFactory初始化完成阶段（核心方法）** <br>
#### AbstractApplicationContext>finishBeanFactoryInitialization(beanFactory); <br>

// 初始化上下文的 conversion service <br>
a. BeanFactory关联ConversionService Bean（如果存在的话）<br>

b. 添加StringValueResolver对象提供处理占位符操作 <br>

c. 依赖查找LoadTimeWeaverAware Bean <br>

// 停止使用类型匹配的临时类加载器 <br>
d. BeanFactory临时ClassLoader置为null <br>

// 允许缓存所有bean定义元数据，而不期待进一步的更改 <br>
e. BeanFactory冻结配置 <br>

// 实例化剩余的（非懒加载）的单例 <br>
// 懒加载：指的是bean单例不是在Spring容器初始化的时候就创建的，而是在要使用该bean的时候，才会创建该bean <br>
f. BeanFactory初始化非延迟单例Beans <br>
**核心步骤** ：beanFactory.preInstantiateSingletons(); <br>

**ConcurrentHashMap转POJO <br>
最终是在AbstractAutowireCapableBeanFactory类里的applyPropertyValues方法里完成，<br>
是通过对原属性值进行了一次深拷贝，然后将深拷贝后的属性值填充到bean里的。**<br>

AbstractBeanFactory.getBean(FACTORY_BEAN_PREFIX + beanName)>doGetBean(name, null, null, false)>332行
    
    if (mbd.isSingleton()) {
        sharedInstance = getSingleton(beanName, () -> {
            try {
                return createBean(beanName, mbd, args);
            }
            catch (BeansException ex) {
                // Explicitly remove instance from singleton cache: It might have been put there
                // eagerly by the creation process, to allow for circular reference resolution.
                // Also remove any beans that received a temporary reference to the bean.
                destroySingleton(beanName);
                throw ex;
            }
        });
        beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
    }

中的createBean(beanName, mbd, args)> <br>

doCreateBean(beanName, mbdToUse, args)> <br>

populateBean(beanName, mbd, instanceWrapper)> <br>

applyPropertyValues(beanName, mbd, bw, pvs);<br>



#### 11.Spring容器上下文启动完成阶段 <br>
#### AbstractApplicationContext>finishRefresh(); <br>

a. 清除ResourceCaches缓存: clearResourceCaches(); <br>

b. 初始化LifecycleProcessor对象: initLifecycleProcessor() <br>

c. 调用LifecycleProcessor.onRefresh() 方法 <br>

d. 发布Spring容器上下文已刷新事件：ContextRefreshedEvent <br>

e. 向MBeanServer托管Live Beans（可以通过JConsole查看） <br>

    问：什么是MBeanServer ？

    答：MBeanServer是一个包含所有注册MBean的仓库.它是JMX代理层的核心.
    JMX1.0规范提供一个接口叫 javax.management.MBeanServer. 
    所有管理的在MBean操作通过MBeanServer执行.使用MBeanServer实例,你能够管理所有MBean.
    每一个MBean具有一个唯一标志,叫ObjectName.
    
    问：什么是ObjectName ？

    答：ObjectName (javax.management.ObjectName)是一个类,唯一标志一个在MBeanServer的MBean.
    这个对象名称用于管理应用程序来标志MBean以便操作能够在适当的MBean上被调用.一个对象名称包含两部分.它们是
    
    1.一个域名称
    2.一个没有经过排序的一个或者多个关键属性集
    
    域名称是一个大小写敏感的字符串,这个字符串可以包括任何除[: , = * ?]之外的字符.

[通过JConsole连接JMXServer管理MBean](https://blog.csdn.net/DryKillLogic/article/details/38413657) <br>

#### 12.Spring容器上下文启动阶段 （可以不调用它，调不调用关系不大）<br>
#### AbstractApplicationContext>start(); <br>

a.启动LifecycleProcessor <br>

b.发布Spring容器上下文已启动事件：ContextStartedEvent事件 <br>

#### 13.Spring容器上下文停止阶段 （可以不调用它，调不调用关系不大）<br>
#### AbstractApplicationContext>stop(); <br>

//与start()方法正好相反 <br>
a. 停止LifecycleProcessor <br>

b. 发布Spring容器上下文已停止事件：ContextStoppedEvent事件 <br>

#### 14.Spring容器上下文关闭阶段 **（可以不调用它，但是规范写法是会调用的）**<br>
#### AbstractApplicationContext>close(); <br>
a. Spring容器设置状态标识：active=false，closed=true <br>


    private final AtomicBoolean closed = new AtomicBoolean();
    this.closed.compareAndSet(false, true)，（写法太美了）
    使用 AtomicBoolean 高效并发处理 “只初始化一次” 的功能要求
    AtomicBoolean赋值的另一种写法
    private final AtomicBoolean active = new AtomicBoolean();
    this.active.set(false);


b. Live Beans JMX撤销托管 <br>

c. 发布Spring容器上下文的已关闭事件 <br>

d.  关闭LifecycleProcessor <br>

e. 销毁Spring Beans <br>

f. 关闭BeanFactory <br>

g. 调用onClose()，让子函数做一些清理工作 <br>
**这是个空方法，需要子类去扩展这个方法** <br>

h.如果ShutdownHook这个线程不为null，remove ShutDown Hook Thread <br>

## 参考文献

[1] [CoderBruis, 深入Spring源码系列（二）——深入Spring容器，通过源码阅读和时序图来彻底弄懂Spring容器（上）](https://blog.csdn.net/coderbruis/article/details/85940756) <br>
[2] [Jim.Sheng, Spring IOC容器生命周期阶段总结](https://blog.csdn.net/shengqianfeng/article/details/112800752) <br>
