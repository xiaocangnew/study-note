### 创建bean：
- 1.如果内存中有，则从内存中取
- 2.获取beanName;  如果内存中没有并且当前beanFactory中没有beanName，则判断parentBeanFactory中是否有，如果有，则从parentBeanFactory中取;如果当前beanFactory中有beanName，合并parent的BeanDefinition
- 3.处理dependsOn属性。 如果有依赖，调用依赖的beanName的getBean
- 4.createBean(beanName, mbd, args);  按照Singleton、Prototype等scope属性，创建bean实例。如果是Singleton，需要在线程安全下创建，如果是Prototype则需要创建新的实例
 - 4.1 创建新的RootBeanDefinition供创建bean使用，处理lookup-method或replace-method，调用InstantiationAwareBeanPostProcessor 进行前后处理
 - 4.2 AbstractAutowireCapableBeanFactory.doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) 创建bean实例
 - 4.3 instanceWrapper = createBeanInstance(beanName, mbd, args); 反射创建bean实例
 - 4.4 调用MergedBeanDefinitionPostProcessor的postProcessMergedBeanDefinition
 - 4.5 完成依赖参数注入populateBean(beanName, mbd, instanceWrapper);如果依赖的bean还未初始化，则调用getBean初始化，然后注入
 - 4.6 initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd);
 
bean默认配置是随容器一起初始化，但如果配置延迟加载的话，bean在第一次被调用的时候初始化
   
流程：步骤4
> 1. 实例化:调用InstanceAwareBeanPostProcessor; postProcessBeforeInstance() ----> instance(实例化) ----> postProcessAfterInstance() ----> postProcessPropertyValues() ---> 设置属性值
> 2. 调用BeanNameAware.setBeanName() ----> BeanFactoryAware.setBeanFactory()
> 3. 初始化：调用BeanPostProcessor；postProcessBeforeInitialization() -----> InitializingBean.afterPropertySet() ,inital-method() ----> postProcessAfterInitialization()
 
### beanFactoryPostProcessor && BeanPostProcessor
-beanFactoryPostProcessor是在读取到BeanDefinition之后， instance(实例化)之前进行的，可以修改bean定义的数据，
-BeanPostProcessor， 初始化前后进行的

### 自己创建bean，注入

@Autowired
private AutowireCapableBeanFactory beanFactory;
beanFactory.autowireBean(channel);
beanFactory.initializeBean(channel, beanName); // 使@PostConstruct生效


### 自己创建bean定义，系统创建bean
- springboot中，自动装配zookeeper，redis等，都是在BeanDefinitionRegistryPostProcessor中加载autoConfig配置类，在afterPropertySet()方法中进行初始化。
- 项目中rabbitmq消费者也是在这里进行多队列扩展的。


实现BeanDefinitionRegistryPostProcessor接口， 向BeanDefinitionRegistry 注册自己的bean定义

@Override
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
     String[] beanNames = registry.getBeanDefinitionNames();
     Map<String, BeanDefinition> toRegister = new HashMap<>();
        for (String beanName : beanNames) {
            BeanDefinition definition = registry.getBeanDefinition(beanName);
            if (definition != null && definition.getBeanClassName() != null) {
                try {
                    Class<?> clazz = Class.forName(definition.getBeanClassName());
                    if (clazz != null && MessageHandler.class.isAssignableFrom(clazz)) {
                        RabbitRetryableHandler handler = clazz.getAnnotation(RabbitRetryableHandler.class);
                            toRegister.putAll(prepareForDelayedType(handler));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
     toRegister.forEach(registry::registerBeanDefinition);
}

private Map<String, BeanDefinition> prepareForDelayedType(RabbitRetryableHandler handler) {
     Map<String, BeanDefinition> toRegister = new HashMap<>();
     for (String queue : RabbitRetryableHandlerUtils.getQueueNames(handler, groupIndex)) {
            String delayedExchangeName = queue + Consts.DELAYED_EXCHANGE_SUFFIX;

            GenericBeanDefinition delayedExchange = newCustomExchange(delayedExchangeName);
            GenericBeanDefinition workQueue = newQueue(queue, null);
            GenericBeanDefinition binding = newBinding(queue, delayedExchangeName, "#");
            
            toRegister.put(queue + "-delayedExchange", delayedExchange);
            toRegister.put(queue + "-WorkQueue", workQueue);
            toRegister.put(queue + "-Binding", binding);
        }
        return toRegister;
    }
}