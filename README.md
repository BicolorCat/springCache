http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#cache-annotations-cacheable
spring-4.3.7


Cache Abstraction

36.1 Introduction

Since version 3.1, Spring Framework provides support for transparently adding caching into an existing Spring application. Similar to the transaction support, the caching abstraction allows consistent use of various caching solutions with minimal impact on the code.

As from Spring 4.1, the cache abstraction has been significantly improved with the support of JSR-107 annotations and more customization options.

自从3.1版本以来,Spring框架提供了一个对已知的spirng应用添加缓存的明确支持.类似对于事物的支持,缓存抽象在对代码影响最小的基础下,允许对各种各种的缓存解决方案的一致使用.

从Spring 4.1开始,缓存抽象在JSR-107的支持下有了明显的提升且有了更多的自定义选项.

36.2 Understanding the cache abstraction

Cache vs Buffer

The terms "buffer" and "cache" tend to be used interchangeably; note however they represent different things. A buffer is used traditionally as an intermediate temporary store for data between a fast and a slow entity. As one party would have to wait for the other affecting performance, the buffer alleviates this by allowing entire blocks of data to move at once rather then in small chunks. The data is written and read only once from the buffer. Furthermore, the buffers are visible to at least one party which is aware of it.

A cache on the other hand by definition is hidden and neither party is aware that caching occurs.It as well improves performance but does that by allowing the same data to be read multiple times in a fast fashion.

A further explanation of the differences between two can be found here.

缓存 vs 缓冲

术语"缓存"和"缓冲"往往可以互换使用;然后它们代表不同的东西.传统上缓冲被用作与快和慢实体间的中间临时存储器.由于一方需等待另乙方从而影响性能,缓冲通过允许马上移动整个数据块而非一小快来起到缓解目的.数据只从缓冲区写入和读取一次.此外, 缓冲器是可见的至少一方知道它..

缓存从其他角度来定义是被隐藏且任何一方都未意识到缓存出现过.同样的它也提升性能,但是通过允许快速读取相同数据而实现的

进一步解释两者之间的不同可以在下面查看

At its core, the abstraction applies caching to Java methods, reducing thus the number of executions based on the information available in the cache. That is, each time a targeted method is invoked, the abstraction will apply a caching behavior checking whether the method has been already executed for the given arguments. If it has, then the cached result is returned without having to execute the actual method; if it has not, then method is executed, the result cached and returned to the user so that, the next time the method is invoked, the cached result is returned. This way, expensive methods (whether CPU or IO bound) can be executed only once for a given set of parameters and the result reused without having to actually execute the method again. The caching logic is applied transparently without any interference to the invoker.

在其核心,抽象应用于java方法,从而减少基于可用信息在缓冲中执行的次数.也就是说,每当一个目标方法被调用,该抽象将会请求一个缓存行为来检查在给定的参数下该方法是否已经被执行过.如果是,缓存结果在不需要执行该方法下就返回了,如果否,方法将会被执行,返回结果会被缓存并返回用调用者,下次这个方法被调用时,该缓存结果会被返回.通过该方式,消耗高的方法(不论cpu型还是IO型)在给定的一系列参数下只被执行一次并且不需要再次执行该方法,返回值就能被重复使用.缓存逻辑被应用时对调用者没有任何干扰.

Important

Obviously this approach works only for methods that are guaranteed to return the same output (result) for a given input (or arguments) no matter how many times it is being executed.

显而易见的,该方法只适用于不管执行多少次，确保给定的输入(或者参数)返回同类型的输出(结果集)的方法.

Other cache-related operations are provided by the abstraction such as the ability to update the content of the cache or remove one of all entries. These are useful if the cache deals with data that can change during the course of the application.

Just like other services in the Spring Framework, the caching service is an abstraction (not a cache implementation) and requires the use of an actual storage to store the cache data - that is, the abstraction frees the developer from having to write the caching logic but does not provide the actual stores. This abstraction is materialized by the org.springframework.cache.Cache and org.springframework.cache.CacheManager interfaces.

There are a few implementations of that abstraction available out of the box: JDK java.util.concurrent.ConcurrentMap based caches, Ehcache 2.x, Gemfire cache, Caffeine, Guava caches and JSR-107 compliant caches (e.g. Ehcache 3.x). See Section 36.7, “Plugging-in different back-end caches” for more information on plugging in other cache stores/providers.

例如更新缓存内容或者移除某个缓存相关缓存操作被提供与该抽象.若在应用使用期间缓存处理数据，那么都是有用的.

就如同Spring框架中的其他服务,缓存服务是一个抽象(并不是一个实现)且需要一个真实的存储器其存储数据-也就是说,该抽象使得开发人员脱离于必须写缓存逻辑,但还需要提供真实存储.该抽象通过org.springframework.cache.Cache and org.springframework.cache.CacheManager 接口来实现.

这有些该抽象可用的开源实现:JDK java.util.concurrent.ConcurrentMap based caches, Ehcache 2.x, Gemfire cache, Caffeine(咖啡因), Guava(番石榴) caches and JSR-107 兼容缓存 (e.g. Ehcache 3.x).参见36.7章节, “Plugging-in different back-end caches” 获取其他缓存存储/提供者在插件上的信息

Important

The caching abstraction has no special handling of multi-threaded and multi-process environments as such features are handled by the cache implementation. .

该缓存抽象对于多线程和多进程环境没有特殊处理,类似这些特性由缓存实现处理.

If you have a multi-process environment (i.e. an application deployed on several nodes), you will need to configure your cache provider accordingly. Depending on your use cases, a copy of the same data on several nodes may be enough but if you change the data during the course of the application, you may need to enable other propagation mechanisms.

Caching a particular item is a direct equivalent of the typical get-if-not-found-then- proceed-and-put-eventually code blocks found with programmatic cache interaction: no locks are applied and several threads may try to load the same item concurrently. The same applies to eviction: if several threads are trying to update or evict data concurrently, you may use stale data. Certain cache providers offer advanced features in that area, refer to the documentation of the cache provider that you are using for more details.

To use the cache abstraction, the developer needs to take care of two aspects:

caching declaration - identify the methods that need to be cached and their policy cache configuration - the backing cache where the data is stored and read from

如果你有一个多进程环境(i.e. 部署在不同节点的一个应用),因此你需要配置不同的缓存提供器.依据于你的用例,不同节点上，相同的数据副本是足够的,但是在应用使用过程中改变了数据,你可能需要启用其他传播机制.

缓存唯一的内容直接等价于典型的get-if-not-found-then- proceed-and-put-eventually发现代码块与缓存交互编程:没有锁的应用以及多线程应用可能同时加载相同数据.这同样适用于清空操作:假使若干线程同时尝试更新或者清空数据,你有可能使用老的数据.请参阅您正在使用的详细信息的缓存提供程序文档,确认该缓存提供者提供高级特性.

要使用缓存抽象，开发者需要考虑两个方面：

缓存声明-确认需要缓存的方法以及它们的策略 缓存配置-数据存储和读取的备份缓存

36.3 Declarative annotation-based caching

For caching declaration, the abstraction provides a set of Java annotations:

@Cacheable triggers cache population @CacheEvict triggers cache eviction @CachePut updates the cache without interfering with the method execution @Caching regroups multiple cache operations to be applied on a method @CacheConfig shares some common cache-related settings at class-level Let us take a closer look at each annotation:

声明缓存注解,该抽象提供了一系列的java注解:

@Cacheable 触发缓存入口 @CacheEvict 触发缓存清空 @CachePut 在不需要方法执行的情况下更新缓存 @Caching 将多个缓存重组应用到一个方法 @CacheConfig 共享类级别缓存相关设置 让我们仔细看看每个注释：

36.3.1 @Cacheable annotation

As the name implies, @Cacheable is used to demarcate methods that are cacheable - that is, methods for whom the result is stored into the cache so on subsequent invocations (with the same arguments), the value in the cache is returned without having to actually execute the method. In its simplest form, the annotation declaration requires the name of the cache associated with the annotated method:

@Cacheable("books") public Book findBook(ISBN isbn) {...}

In the snippet above, the method findBook is associated with the cache named books. Each time the method is called, the cache is checked to see whether the invocation has been already executed and does not have to be repeated. While in most cases, only one cache is declared, the annotation allows multiple names to be specified so that more than one cache are being used. In this case, each of the caches will be checked before executing the method - if at least one cache is hit, then the associated value will be returned:

顾名思义,@Cacheable被用于标定方法缓存了-也就是说,方法被执行的结果被存储在缓存中以便后续调用(使用相同的参数),缓存值在不需要执行确切的方法下就能返回.在最简单的形式下,注解声明需要缓存名字和被注解的方法名字有所关联:

@Cacheable("books")

public Book findBook(ISBN isbn) {...}

在上面的代码片段中,方法findBook和被叫做books的缓存相关联.每当该方法被调用,该缓存检查方法是否已经被调用过并不必重复调用.尽管在大多数的情况下,只有一个缓存需要声明,注解允许多个名字以便多个缓存被使用.在这种情况下,每一个缓存将会被检查在执行方法前-若至少有一个缓存命中,相关联的值将会被返回:

All the other caches that do not contain the value will be updated as well even though the cached method was not actually executed.

尽管被缓存的方法没有确切的执行,但未包含该返回值的其他缓存也将会被更新

@Cacheable({"books", "isbns"})

public Book findBook(ISBN isbn) {...}

Default Key Generation

Since caches are essentially key-value stores, each invocation of a cached method needs to be translated into a suitable key for cache access. Out of the box, the caching abstraction uses a simple KeyGenerator based on the following algorithm:

If no params are given, return SimpleKey.EMPTY. If only one param is given, return that instance. If more the one param is given, return a SimpleKey containing all parameters.

This approach works well for most use-cases; As long as parameters have natural keys and implement valid hashCode() and equals() methods. If that is not the case then the strategy needs to be changed.

To provide a different default key generator, one needs to implement the org.springframework.cache.interceptor.KeyGenerator interface.

默认密钥生成器

由于缓存本质上是键值存储,所以每个缓存方法的调用都需要转换成缓存访问的合适密钥.开箱即用,缓存抽象基本以下算法使用了一个简单的密钥生成器:

如没有给定参数,返回SimpleKey.EMPTY. 如只给定一个参数,返回该参数. 如给定了多个参数,则返回一个包含所有参数的SimpleKey.

这种方法适用于大多种情况;只要参数有自然键,并实现合法的hashCode()和equals()方法.如果不是这种情况那么该策略需要改变

为了提供不同的默认密钥生成器,需要实现该接口org.springframework.cache.interceptor.KeyGenerator

The default key generation strategy changed with the release of Spring 4.0. Earlier versions of Spring used a key generation strategy that, for multiple key parameters, only considered the hashCode() of parameters and not equals(); this could cause unexpected key collisions (see SPR-10237 for background). The new 'SimpleKeyGenerator' uses a compound key for such scenarios.

If you want to keep using the previous key strategy, you can configure the deprecated org.springframework.cache.interceptor.DefaultKeyGenerator class or create a custom hash-based 'KeyGenerator' implementation.

当spring4.0发布时，默认的密钥生产器策略也随之变化.早期Spring版本使用如下密钥生产策略,对于多个键参数,只考虑参数的hashCode()而忽视了equals();这可能会引起不期望的键冲突(参阅背景资料SPR-10237).新的'SimpleKeyGenerator'使用复合键来应对类似的场景.

假如想保留以前的键策略,你可以配置过时的类org.springframework.cache.interceptor.DefaultKeyGenerator或者创建基于自定义的基于hash的'KeyGenerator'

Custom Key Generation Declaration

Since caching is generic, it is quite likely the target methods have various signatures that cannot be simply mapped on top of the cache structure. This tends to become obvious when the target method has multiple arguments out of which only some are suitable for caching (while the rest are used only by the method logic). For example:

@Cacheable("books") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

At first glance, while the two boolean arguments influence the way the book is found, they are no use for the cache. Further more what if only one of the two is important while the other is not?

自定义键生成器声明 由于缓存是通用的,很可能目标方法有各种签名,不能简单的映射在缓存结构顶部.当目标方法有多个参数且只有一些适合缓存(而其余的只适合方法本身逻辑),在这一点尤其明显,举个例子:

@Cacheable("books")

public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

一瞥之下,虽然这个两个布尔参数影响找到书的方式,但他们是无用的缓存.更进一步看如果只有一个是重要的而另一个不是呢?

For such cases, the @Cacheable annotation allows the user to specify how the key is generated through its key attribute. The developer can use SpEL to pick the arguments of interest (or their nested properties), perform operations or even invoke arbitrary methods without having to write any code or implement any interface. This is the recommended approach over the default generator since methods tend to be quite different in signatures as the code base grows; while the default strategy might work for some methods, it rarely does for all methods.

对于这种情况,@Cacheable注解允许用户通过键的属性来制定键是如何生成的.开发人员可以使用SpEl抽取感兴趣的参数(或者他们内嵌的属性),执行操作或者调用任意的方法且在不需要写任何代码、实现、接口的情况下.这是默认的生成器推荐的方法，因为在代码增长的过程中，签名的方法往往会有很大的不同；而默认的策略可能对某些方法有效，但对于所有的方法起很少的作用。

Below are some examples of various SpEL declarations - if you are not familiar with it, do yourself a favor and read Chapter 10, Spring Expression Language (SpEL):

@Cacheable(cacheNames="books", key="#isbn") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@Cacheable(cacheNames="books", key="#isbn.rawNumber") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@Cacheable(cacheNames="books", key="T(someType).hash(#isbn)") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

The snippets above show how easy it is to select a certain argument, one of its properties or even an arbitrary (static) method.

If the algorithm responsible to generate the key is too specific or if it needs to be shared, you may define a custom keyGenerator on the operation. To do this, specify the name of the KeyGenerator bean implementation to use:

@Cacheable(cacheNames="books", keyGenerator="myKeyGenerator") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed) [Note]

The key and keyGenerator parameters are mutually exclusive and an operation specifying both will result in an exception.

以下有各种SpEl声明的例子-如果你对此不熟悉,阅读第10章会有所帮助,Spring表达式(SpEl):

@Cacheable(cacheNames="books", key="#isbn") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@Cacheable(cacheNames="books", key="#isbn.rawNumber") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@Cacheable(cacheNames="books", key="T(someType).hash(#isbn)") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

上面的代码片段表明他很非常容易选择一个特定的参数,它的属性中的一个或者甚至于一个任意的静态方法.

如果该算法负责生产的键过于明确或者需要共享,你需要定义一个自定义键生成器在这项操作上.要做到这一点,指定要使用的KeyGenerator bean实现的名称:

@Cacheable(cacheNames="books", keyGenerator="myKeyGenerator") public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

键和键生成器属性是互斥的，一个指定的操作会导致异常

Default Cache Resolution

Out of the box, the caching abstraction uses a simple CacheResolver that retrieves the cache(s) defined at the operation level using the configured CacheManager.

To provide a different default cache resolver, one needs to implement the org.springframework.cache.interceptor.CacheResolver interface.

默认缓存决议

开箱即用,缓存抽象使用一个简单的CacheResolver,检索缓存在操作层面定义使用配置CacheManager.

提供不同的默认缓存解析器,需要实现接口org.springframework.cache.interceptor.CacheResolver

Custom cache resolution

The default cache resolution fits well for applications working with a single CacheManager and with no complex cache resolution requirements.

For applications working with several cache managers, it is possible to set the cacheManager to use per operation:

@Cacheable(cacheNames="books", cacheManager="anotherCacheManager") public Book findBook(ISBN isbn) {...}

It is also possible to replace the CacheResolver entirely in a similar fashion as for key generation. The resolution is requested for every cache operation, giving a chance to the implementation to actually resolve the cache(s) to use based on runtime arguments:

@Cacheable(cacheResolver="runtimeCacheResolver") public Book findBook(ISBN isbn) {...} [Note]

Since Spring 4.1, the value attribute of the cache annotations are no longer mandatory since this particular information can be provided by the CacheResolver regardless of the content of the annotation. Similarly to key and keyGenerator, the cacheManager and cacheResolver parameters are mutually exclusive and an operation specifying both will result in an exception as a custom CacheManager will be ignored by the CacheResolver implementation. This is probably not what you expect.

自定义缓存解决方案

默认缓存器非常适用于单一的CacheManager并且没有复杂缓存器需求的应用程序.

对于有多个缓存管理器的应用,针对每个操作设置Cachemanager:

@Cacheable(cacheNames="books", cacheManager="anotherCacheManager")

public Book findBook(ISBN isbn) {...}

同样的它也可以以同样的方式彻底替换CacheResolver就如密钥生成器一样.对每个缓存操作有要求该解决方案,这给了实现实际解决基于运行时参数使用缓存的机会:

@Cacheable(cacheResolver="runtimeCacheResolver")

public Book findBook(ISBN isbn) {...}

自spring4.1以来,CacheResolver不论注解的内容以否,特定的信息都能被提供,缓存的值属性注解都不需要托管.类似于key和keyGenerator,cacheManager和cacheResolver属性是互斥的,一项指定操作将会导致异常,自定义cacheManager会被CacheResolver实现忽视.这可能不是你所期望的.

Synchronized caching

In a multi-threaded environment, certain operations might be concurrently invoked for the same argument (typically on startup). By default, the cache abstraction does not lock anything and the same value may be computed several times, defeating the purpose of caching.

For those particular cases, the sync attribute can be used to instruct the underlying cache provider to lock the cache entry while the value is being computed. As a result, only one thread will be busy computing the value while the others are blocked until the entry is updated in the cache.

@Cacheable(cacheNames="foos", sync="true") public Foo executeExpensiveOperation(String id) {...} [Note] This is an optional feature and your favorite cache library may not support it. All CacheManager implementations provided by the core framework support it. Check the documentation of your cache provider for more details.

同步缓存

在多线程环境下,某些操作可能使用相同参数同时调用(通常在启动时).默认情况下,缓存抽象不会锁定任何东西,相同值可能会重复计算多次,破环缓存的目的了.

针对这些特定情况,同步属性可以指定底层缓存提供器去锁定缓存入口当值被计算时.其结果是,只有一个线程将会忙于计算值,而其他将会被堵塞直至缓存入口被更新.

@Cacheable(cacheNames="foos", sync="true")

public Foo executeExpensiveOperation(String id) {...}

这是一个可选功能,你最喜欢的缓存库可能不支持它.所有的CachecManager实现提供核心框架支持它.检查你的缓存提供器文档获得更详细的信息.

Conditional caching

Sometimes, a method might not be suitable for caching all the time (for example, it might depend on the given arguments). The cache annotations support such functionality through the condition parameter which takes a SpEL expression that is evaluated to either true or false. If true, the method is cached - if not, it behaves as if the method is not cached, that is executed every time no matter what values are in the cache or what arguments are used. A quick example - the following method will be cached only if the argument name has a length shorter than 32:

@Cacheable(cacheNames="book", condition="#name.length < 32") public Book findBook(String name)

In addition the condition parameter, the unless parameter can be used to veto the adding of a value to the cache. Unlike condition, unless expressions are evaluated after the method has been called. Expanding on the previous example - perhaps we only want to cache paperback books:

@Cacheable(cacheNames="book", condition="#name.length < 32", unless="#result.hardback") public Book findBook(String name)

The cache abstraction supports java.util.Optional, using its content as cached value only if it present. #result always refers to the business entity and never on a supported wrapper so the previous example can be rewritten as follows:

@Cacheable(cacheNames="book", condition="#name.length < 32", unless="#result.hardback") public Optional<Book> findBook(String name) Note that result still refers to Book and not Optional.

有条件的缓存

有时候,一个方法可能不适合与所有场景(例如,它可能依赖于给定的参数).缓存注解支持通过使用SpEl表达式来求得条件表达式的真或假.若真,方法被缓存-若假,方法不会被缓存,不论值否是在缓存中亦或参数被使用,每次都被会执行.一个快速的例子-假若参数值长度小于32则下面的方法会被缓存:

@Cacheable(cacheNames="book", condition="#name.length < 32")

public Book findBook(String name)

此外,条件参数,unless属性能够被用于否决添加到缓存的中指.与条件不同,unless表达式在方法调用之后被使用.扩展上一个例子-也许我们只想缓存平装书:

@Cacheable(cacheNames="book", condition="#name.length < 32", unless="#result.hardback")

public Book findBook(String name)

该缓存抽象支持java.util.Optional,若present(注:Optional方法isPresent())则用它的内容当作缓存值. [#result]总是关联业务实体并且不支持包装器,所以上一个例子可以被重写为下面这样:

@Cacheable(cacheNames="book", condition="#name.length < 32", unless="#result.hardback")

public Optional<Book> findBook(String name)

注意该result仍然关联到Book而非Optional

Available caching SpEL evaluation context

Each SpEL expression evaluates again a dedicated context. In addition to the build in parameters, the framework provides dedicated caching related metadata such as the argument names. The next table lists the items made available to the context so one can use them for key and conditional computations:

Table 36.1. Cache SpEL available metadata

image

SpEl语境下可用缓存

每一个SpEl表达式又有专用的上下文.除了构建参数,框架提供了专门的缓存关联元数据,如参数名.下表列出了上下文中可用的项目,以便在键和条件计算中使用它们:

表格36.1 SpEl可用元数据缓存

Name	Location	Description	Example
methodName	root object		#root.methodName
method	root object	The name of the method being invoked	#root.methodName
target	root object	The name of the method being invoked	#root.methodName
targetClass	root object	The name of the method being invoked	#root.methodName
args	root object	The name of the method being invoked	#root.methodName
caches	root object	The name of the method being invoked	#root.methodName
argument name	evaluation context	The name of the method being invoked	#root.methodName
result	evaluation context	The name of the method being invoked	#root.methodName
36.3.2 @CachePut annotation

For cases where the cache needs to be updated without interfering with the method execution, one can use the @CachePut annotation. That is, the method will always be executed and its result placed into the cache (according to the @CachePut options). It supports the same options as @Cacheable and should be used for cache population rather than method flow optimization:

@CachePut(cacheNames="book", key="#isbn") public Book updateBook(ISBN isbn, BookDescriptor descriptor)

[Important]	Important Note that using @CachePut and @Cacheable annotations on the same method is generally strongly discouraged because they have different behaviors. While the latter causes the method execution to be skipped by using the cache, the former forces the execution in order to execute a cache update. This leads to unexpected behavior and with the exception of specific corner-cases (such as annotations having conditions that exclude them from each other), such declaration should be avoided. Note also that such condition should not rely on the result object (i.e. the #result variable) as these are validated upfront to confirm the exclusion.

36.3.2 @CachePut 注解

如果缓存需要更新在方法没有干扰的情况下执行,可以使用@CachePut注解.也就是说,该方法总是会被执行并且结果值会放入缓存(依据@CachePut选项).它和@Cacheable支持相同的选项,并且被用作于缓存入口强于方法流程优化：

@CachePut(cacheNames="book", key="#isbn")

public Book updateBook(ISBN isbn, BookDescriptor descriptor)

Important

注意:强烈不鼓励使用@CachePut和@Cacheabl注解在同一方法上,因为他们有不同的行为.虽然后者会通过使用缓存引起方法执行被跳过,而前者会强制执行方法去为了更新缓存数据.这将导致不期望的行为，且特定的行为会有异常(如注释有条件,彼此被剔除), 类似声明要被避免.注意,类似条件不应依赖结果对象(#result变量),因此在前期要验证确认排除.

36.3.3 @CacheEvict annotation

The cache abstraction allows not just population of a cache store but also eviction. This process is useful for removing stale or unused data from the cache. Opposed to @Cacheable, annotation @CacheEvict demarcates methods that perform cache eviction, that is methods that act as triggers for removing data from the cache. Just like its sibling, @CacheEvict requires specifying one (or multiple) caches that are affected by the action, allows a custom cache and key resolution or a condition to be specified but in addition, features an extra parameter allEntries which indicates whether a cache-wide eviction needs to be performed rather then just an entry one (based on the key):

@CacheEvict(cacheNames="books", allEntries=true) public void loadBooks(InputStream batch)

This option comes in handy when an entire cache region needs to be cleared out - rather then evicting each entry (which would take a long time since it is inefficient), all the entries are removed in one operation as shown above. Note that the framework will ignore any key specified in this scenario as it does not apply (the entire cache is evicted not just one entry).

One can also indicate whether the eviction should occur after (the default) or before the method executes through the beforeInvocation attribute. The former provides the same semantics as the rest of the annotations - once the method completes successfully, an action (in this case eviction) on the cache is executed. If the method does not execute (as it might be cached) or an exception is thrown, the eviction does not occur. The latter ( beforeInvocation=true) causes the eviction to occur always, before the method is invoked - this is useful in cases where the eviction does not need to be tied to the method outcome.

It is important to note that void methods can be used with @CacheEvict - as the methods act as triggers, the return values are ignored (as they don’t interact with the cache) - this is not the case with @Cacheable which adds/updates data into the cache and thus requires a result.

36.3.3 @CacheEvict声明

缓存抽象不仅仅允许缓存存储,同样适用于清除.这个过程适用于从缓存中移除过期或者未使用的数据.和@Cacheable相反,@CacheEvict注解划定方法执行清除缓存,也就是说,方法类似一个从缓存中移除数据的触发器.就像它的同胞,@CacheEvict需要指定一个或多个缓存被影响,允许一个自定义缓存和键决议或者被指定添加的一个条件，除此之外,额外特性参数allEntries指出一个cache-wide清空是否需要执行而不是一个记录(基于键):

@CacheEvict(cacheNames="books", allEntries=true)

public void loadBooks(InputStream batch)

这个选项是在方便的时候清除整个缓存区域-而不是每个条目(由于它效率低会花费很长一段时间),所有的条目将会被在一个操作中被移除如上所示.值得注意的是该框架在这个场景下 指定为忽略任何键(所有缓存都被清空而不仅仅是一个).

还可以通过beforeInvocation属性来指定清除发生在方法执行之后(默认)或者之前.前者提供了相同语义注释的其余部分-一旦方法成功完成,在这上的缓存操作(这个例子是eviction)将被执行.若方法未被执行(可能被缓存)或者抛出了异常,清空不会发生.后者(beforeInvocation=true)在方法调用前一直都会发生清除-这会有利于清除操作不需要以方法结果为依赖.

值得注意的是,void的方法也能使用@CacheEvict-这种方法作为触发器,返回值被忽视(因为他们不需要和缓存交互)-这并不是@Cacheable一样需要添加/更新数据到缓存中且需要一个返回值.

36.3.4 @Caching annotation

There are cases when multiple annotations of the same type, such as @CacheEvict or @CachePut need to be specified, for example because the condition or the key expression is different between different caches. @Caching allows multiple nested @Cacheable, @CachePut and @CacheEvict to be used on the same method:

@Caching(evict = { @CacheEvict("primary"), @CacheEvict(cacheNames="secondary", key="#p0") }) public Book importBooks(String deposit, Date date)

36.3.4 @Caching 注解

在某些情况下,多个注释的类型相同,例如@CacheEvict或者@CachePut需要被制定,举个例子,键表达式或者条件在不同的缓存中是不同的.@Caching允许同个方法上嵌入多个@Cacheable, @CachePut和@CacheEvict.

@Caching(evict = { @CacheEvict("primary"), @CacheEvict(cacheNames="secondary", key="#p0") })

public Book importBooks(String deposit, Date date)

36.3.5 @CacheConfig annotation

So far we have seen that caching operations offered many customization options and these can be set on an operation basis. However, some of the customization options can be tedious to configure if they apply to all operations of the class. For instance, specifying the name of the cache to use for every cache operation of the class could be replaced by a single class-level definition. This is where @CacheConfig comes into play.

public class BookRepositoryImpl implements BookRepository {

    @Cacheable
    public Book findBook(ISBN isbn) {...}
}
@CacheConfig is a class-level annotation that allows to share the cache names, the custom KeyGenerator, the custom CacheManager and finally the custom CacheResolver. Placing this annotation on the class does not turn on any caching operation.

An operation-level customization will always override a customization set on @CacheConfig. This gives therefore three levels of customizations per cache operation:

Globally configured, available for CacheManager, KeyGenerator At class level, using @CacheConfig At the operation level

36.3.5 @CacheConfig 注解

到目前为止,我们已经看到缓存操作提供了很多的自定义选项并且它们能都依据操作来设置.然后，所以所有的类操作来说,一些自定义操作配置会单调.例如,指定类的每一项缓存操作的缓存名称能够被替换为一个类级别的定义.这就是@CacheConfig的作用.

@CacheConfig("books")
public class BookRepositoryImpl implements BookRepository {

    @Cacheable
    public Book findBook(ISBN isbn) {...}
}
@CacheConfig一一个类级别的注解允许分享缓存名称,自定义KeyGeneratorm,自定义CacheManager,自定义CacheResolver.在类上放置该注解不会开启任何缓存操作.

全局配置,可用于CacheManager,KeyGenerator
类级别,使用@CacheConfig
在操作层面
36.3.6 Enable caching annotations

It is important to note that even though declaring the cache annotations does not automatically trigger their actions - like many things in Spring, the feature has to be declaratively enabled (which means if you ever suspect caching is to blame, you can disable it by removing only one configuration line rather than all the annotations in your code).

To enable caching annotations add the annotation @EnableCaching to one of your @Configuration classes:

@Configuration @EnableCaching public class AppConfig { } Alternatively for XML configuration use the cache:annotation-driven element:

<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:cache="http://www.springframework.org/schema/cache" xsi:schemaLocation=" http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/cache http://www.springframework.org/schema/cache/spring-cache.xsd">

<cache:annotation-driven />
</beans> Both the cache:annotation-driven element and @EnableCaching annotation allow various options to be specified that influence the way the caching behavior is added to the application through AOP. The configuration is intentionally similar with that of @Transactional:

[Note] Advanced customizations using Java config require to implement CachingConfigurer, refer to the javadoc for more details.

36.3.6 启用缓存注解

需要注意的是,即使声明缓存注解也不会触发他们的行为-类似spring的很多事物,该特性必须声明为可用(也就意味着如果你怀疑缓存是起因,你可以通过移除一个配置项不启用它好过于移除你代码中的所有缓存).

@Configuration
@EnableCaching
public class AppConfig {}
另一种使用缓存元素:annotation-driven的xml配置

<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:cache="http://www.springframework.org/schema/cache"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/cache http://www.springframework.org/schema/cache/spring-cache.xsd">

        <cache:annotation-driven />

</beans>
缓存:annotation-driven元素和@EnableCaching声明允许各种选项被指定,会影响通过AOP添加到应用中的缓存行为.该配置有意的和@Transactional类似:

更高级的自定义设置需要实现CacheingConfigure通过使用java来配置,关联the javadoc for more details.

Table 36.2. Cache annotation settings

![image](http://yangege.b0.upaiyun.com/11e63c3a555bf.jpg)

<cache:annotation-driven/> only looks for @Cacheable/@CachePut/@CacheEvict/@Caching on beans in the same application context it is defined in. This means that, if you put <cache:annotation-driven/> in a WebApplicationContext for a DispatcherServlet, it only checks for beans in your controllers, and not your services. See Section 22.2, “The DispatcherServlet” for more information.

Method visibility and cache annotations

When using proxies, you should apply the cache annotations only to methods with public visibility. If you do annotate protected, private or package-visible methods with these annotations, no error is raised, but the annotated method does not exhibit the configured caching settings. Consider the use of AspectJ (see below) if you need to annotate non-public methods as it changes the bytecode itself.


Spring recommends that you only annotate concrete classes (and methods of concrete classes) with the @Cache* annotation, as opposed to annotating interfaces. You certainly can place the @Cache* annotation on an interface (or an interface method), but this works only as you would expect it to if you are using interface-based proxies. The fact that Java annotations are not inherited from interfaces means that if you are using class-based proxies ( proxy-target-class="true") or the weaving-based aspect ( mode="aspectj"), then the caching settings are not recognized by the proxying and weaving infrastructure, and the object will not be wrapped in a caching proxy, which would be decidedly bad.


In proxy mode (which is the default), only external method calls coming in through the proxy are intercepted. This means that self-invocation, in effect, a method within the target object calling another method of the target object, will not lead to an actual caching at runtime even if the invoked method is marked with @Cacheable - considering using the aspectj mode in this case. Also, the proxy must be fully initialized to provide the expected behaviour so you should not rely on this feature in your initialization code, i.e. @PostConstruct.



Table 36.2. 缓存注解设置


XML Attribute | Annotation Attribute | Default | Description
-----|------|------|------|---
cache-manager | N/A (See CachingConfigurer javadocs) | cacheManager | Name of cache manager to use. A default CacheResolver will be initialized behind the scenes with this cache manager (or `cacheManager`if not set). For more fine-grained management of the cache resolution, consider setting the 'cache-resolver' attribute.
cache-resolver | N/A (See CachingConfigurer javadocs) | A SimpleCacheResolver using the configured cacheManager.| The bean name of the CacheResolver that is to be used to resolve the backing caches. This attribute is not required, and only needs to be specified as an alternative to the 'cache-manager' attribute.
key-generator | N/A (See CachingConfigurer javadocs) | SimpleKeyGenerator | Name of the custom key generator to use.
error-handler | N/A (See CachingConfigurer javadocs) | SimpleCacheErrorHandler | Name of the custom cache error handler to use. By default, any exception throw during a cache related operations are thrown back at the client.
mode | mode | proxy | The default mode "proxy" processes annotated beans to be proxied using Spring’s AOP framework (following proxy semantics, as discussed above, applying to method calls coming in through the proxy only). The alternative mode "aspectj" instead weaves the affected classes with Spring’s AspectJ caching aspect, modifying the target class byte code to apply to any kind of method call. AspectJ weaving requires spring-aspects.jar in the classpath as well as load-time weaving (or compile-time weaving) enabled. (See the section called “Spring configuration” for details on how to set up load-time weaving.)
proxy-target-class | proxyTargetClass | false | Applies to proxy mode only. Controls what type of caching proxies are created for classes annotated with the @Cacheable or @CacheEvict annotations. If the proxy-target-class attribute is set to true, then class-based proxies are created. If proxy-target-class is false or if the attribute is omitted, then standard JDK interface-based proxies are created. (See Section 11.6, “Proxying mechanisms” for a detailed examination of the different proxy types.)
order | order | Ordered.LOWEST_PRECEDENCE | Defines the order of the cache advice that is applied to beans annotated with @Cacheable or @CacheEvict. (For more information about the rules related to ordering of AOP advice, see the section called “Advice ordering”.) No specified ordering means that the AOP subsystem determines the order of the advice.

<cache:annotation-driven/> 只会在同个应用上下文内在bean内定义的@Cacheable/@CachePut/@CacheEvict/@Caching.也就是说,若为了DispatcherServlet在WebApplicationContext里放置<cache:annotation-driven/>,它只会在你的Controllers里做检查,并不是你的service.参见章节[Section 22.2, “The DispatcherServlet” ](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#mvc-servlet)获取更多信息

方法可见性和缓存注解

当使用代理时,应用缓存注解仅当适用于public的方法.若对protected,private或者package-visible的方法进行注解,不会引发错误,但是注释的方法不会显示已配置的缓存设置.考虑使用AspectJ(如下),若你必须对非public方法进行注解,则要改变字节码本身.


spring建议你只能使用[@Cache*]注解来注解具体的类(具体的方法),与注解接口是截然不同的.你确认放置[@Cache*]注解于接口上(或者接口方法),但如果您使用的是基于接口的代理，这将如您所期望的那样工作.但事实是java注解并没有继承接口,意味着你使用基于类的代理(proxy-target-class="true")或者基于编织aspect(mode="aspectj"),然后缓存设置通过代理和编制基础设施未被认识,对象不被包裹在一个缓存代理中,这绝对是糟糕的.

在代理模式中(默认模式),只有通过代理传入的方法调用被拦截,这意味着自调用,事实上,一个目标对象方法调用另一个目标对象方法时,将不会在运行时引导到实际的缓存,即使被调用的方法被标记为@Cacheable-考虑使用aspect模式下的这种情况.此外,代理必须完全初始化提供的预期行为,因此你不应依赖此功能在初始化代码中，即 @PostConstruct.


36.3.7 Using custom annotations

Custom annotation and AspectJ

This feature only works out-of-the-box with the proxy-based approach but can be enabled with a bit of extra effort using AspectJ.

The spring-aspects module defines an aspect for the standard annotations only. If you have defined your own annotations, you also need to define an aspect for those. Check AnnotationCacheAspect for an example.

The caching abstraction allows you to use your own annotations to identify what method triggers cache population or eviction. This is quite handy as a template mechanism as it eliminates the need to duplicate cache annotation declarations (especially useful if the key or condition are specified) or if the foreign imports (org.springframework) are not allowed in your code base. Similar to the rest of the stereotype annotations, @Cacheable, @CachePut, @CacheEvict and @CacheConfig can be used as meta-annotations, that is annotations that can annotate other annotations. To wit, let us replace a common @Cacheable declaration with our own, custom annotation:

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Cacheable(cacheNames="books", key="#isbn")
public @interface SlowService {
}

Above, we have defined our own SlowService annotation which itself is annotated with @Cacheable - now we can replace the following code:

@Cacheable(cacheNames="books", key="#isbn")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
with:

@SlowService
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
Even though @SlowService is not a Spring annotation, the container automatically picks up its declaration at runtime and understands its meaning. Note that as mentioned above, the annotation-driven behavior needs to be enabled.


### 36.3.7 使用自定义注解

自定义注解和AspectJ

此功能只能基于代理方法开箱即用,但通过一点额外的努力来使用AspectJ就能启用了.若你定义了你自身注解,你同样也需要定义apsect.查看AnnotationCacheAspect例子.

缓存抽象允许你使用你自己的注解去定义方法触发缓存入口或者清空操作.这会对于模板机制来说非常方便,因为它消除了重复缓存注解声明(特别有助于key或者条件被指定)或者在基于你代码中不被允许的外部引用(org.springframework).类似于其他的构造性注解,@Cacheable, @CachePut, @CacheEvict和@CacheConfig能被用作元注解,也就是该注解能被其他注解所注解.即,让我们用我们自己的注解来替换一个普通的@Cachable声明,自定义注解:

~~~
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Cacheable(cacheNames="books", key="#isbn")
public @interface SlowService {
}
~~~

如上,我们已经定义了我们自己的SlowService注解,且它在机身已经被@Cacheable所注解-现在我们可以替换以下代码:

~~~
@Cacheable(cacheNames="books", key="#isbn")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
~~~

~~~
@SlowService
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
~~~

尽管@SlowService并不是一个Spring注解,容器会在运行时自动获取它的注解并理解它的意思.注意以上提及的,annotation-driven行为需要可用.



36.4 JCache (JSR-107) annotations
Since the Spring Framework 4.1, the caching abstraction fully supports the JCache standard annotations: these are @CacheResult, @CachePut, @CacheRemove and @CacheRemoveAll as well as the @CacheDefaults, @CacheKey and @CacheValue companions. These annotations can be used right the way without migrating your cache store to JSR-107: the internal implementation uses Spring’s caching abstraction and provides default CacheResolver and KeyGenerator implementations that are compliant with the specification. In other words, if you are already using Spring’s caching abstraction, you can switch to these standard annotations without changing your cache storage (or configuration, for that matter).


### 36.4 JCache (JSR-107) 注解

自sping4.1以来,缓存抽象全面支持JCache标准注解:这些@CacheResult, @CachePut, @CacheRemove 和 @CacheRemoveAll同 @CacheDefaults, @CacheKey 和 @CacheValue 的同伴..这些缓存注解能够被正确的使用,不用迁移缓存存储至JSR-107:该内部实现使用Spring的缓存抽象,提供默认的CacheResolver和KeyGenerator实现从而符合规范.换句话说,若你已经使用Spirng的缓存抽象,你可以切换到这些标准注释,而不改变你的缓存存储(或配置,就此而言)


36.4.1 Features summary

For those who are familiar with Spring’s caching annotations, the following table describes the main differences between the Spring annotations and the JSR-107 counterpart:

Table 36.3. Spring vs. JSR-107 caching annotations

![image](http://yangege.b0.upaiyun.com/11e641b2a7541.png)



Spring | JSR-107 | Remark | 
---|---|---|
@Cacheable | @CacheResult | Fairly similar. @CacheResult can cache specific exceptions and force the execution of the method regardless of the content of the cache. |
@CachePut | @CachePut | While Spring updates the cache with the result of the method invocation, JCache requires to pass it as an argument that is annotated with @CacheValue. Due to this difference, JCache allows to update the cache before or after the actual method invocation. |
@CacheEvict | @CacheRemove | Fairly similar. @CacheRemove supports a conditional evict in case the method invocation results in an exception. |
@CacheEvict(allEntries=true) | @CacheRemoveAll | See @CacheRemove |
@CacheConfig | @CacheDefaults | Allows to configure the same concepts, in a similar fashion. |


JCache has the notion of javax.cache.annotation.CacheResolver that is identical to the Spring’s CacheResolver interface, except that JCache only supports a single cache. By default, a simple implementation retrieves the cache to use based on the name declared on the annotation. It should be noted that if no cache name is specified on the annotation, a default is automatically generated, check the javadoc of @CacheResult#cacheName() for more information.

CacheResolver instances are retrieved by a CacheResolverFactory. It is possible to customize the factory per cache operation:

@CacheResult(cacheNames="books", cacheResolverFactory=MyCacheResolverFactory.class)
public Book findBook(ISBN isbn)

For all referenced classes, Spring tries to locate a bean with the given type. If more than one match exists, a new instance is created and can use the regular bean lifecycle callbacks such as dependency injection.

JCache有javax.cache.annotation.CacheResolver的概念,与Spring的CacheResolver接口相同,除了JCache只支持一个单独缓存.
默认情况,一个简单实现检索缓存使用基于注解的名称声明.应该没指出的是,若在注解上没有特定的缓存名称,默认的会自动生成,参阅@CacheResult#cacheName() javadoc获取更多信息

CacheResolve实例被CacheResolveFactory所检索.可以自定义每个缓存操作的工厂:

~~~
@CacheResult(cacheNames="books", cacheResolverFactory=MyCacheResolverFactory.class)
public Book findBook(ISBN isbn)
~~~

对于所有引用类,Spring尝试通过给定的类型去定位一个bean.若大于一个的匹配,一个新的实例会被创建,类似依赖注入能够使用正常bean的生命周期回调.



Keys are generated by a javax.cache.annotation.CacheKeyGenerator that serves the same purpose as Spring’s KeyGenerator. By default, all method arguments are taken into account unless at least one parameter is annotated with @CacheKey. This is similar to Spring’s custom key generation declaration. For instance these are identical operations, one using Spring’s abstraction and the other with JCache:

@Cacheable(cacheNames="books", key="#isbn")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@CacheResult(cacheName="books")
public Book findBook(@CacheKey ISBN isbn, boolean checkWarehouse, boolean includeUsed)

The CacheKeyResolver to use can also be specified on the operation, in a similar fashion as the CacheResolverFactory.

JCache can manage exceptions thrown by annotated methods: this can prevent an update of the cache but it can also cache the exception as an indicator of the failure instead of calling the method again. Let’s assume that InvalidIsbnNotFoundException is thrown if the structure of the ISBN is invalid. This is a permanent failure, no book could ever be retrieved with such parameter. The following caches the exception so that further calls with the same, invalid ISBN, throws the cached exception directly instead of invoking the method again.

@CacheResult(cacheName="books", exceptionCacheName="failures"
             cachedExceptions = InvalidIsbnNotFoundException.class)
public Book findBook(ISBN isbn)


键通过javax.cache.annotation.CacheKeyGenerator被生成,起到和Spring的KeyGenerator相同的目的.默认下情况下,所有方法参数被考虑除非至少有一个参数被@CacheKey注解.这类似于Spring自定义键生成声明.例如,这些都是相同操作,一个使用spring抽象，另外一个使用JCache:
~~~
@Cacheable(cacheNames="books", key="#isbn")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
~~~

~~~
@CacheResult(cacheName="books")
public Book findBook(@CacheKey ISBN isbn, boolean checkWarehouse, boolean includeUsed)
~~~

CacheKeyResolver也能被指定操作.以CacheResolverFactory类似的方式.

JCache能够通过被注解的方法管理异常的抛出:这能阻止缓存更新,但它同样也能缓存异常,作为故障指示器替换再次调用方法.让我们假设InvalidIsbnNotFoundException会被抛出若ISBN的结构是非法的.这是永久性的失败,没有书可以被这样的参数检索.以下缓存一场,因此,进一步的相同请求,非法的ISBN,直接抛出缓存替代再次调用方法.
~~~
@CacheResult(cacheName="books", exceptionCacheName="failures"
             cachedExceptions = InvalidIsbnNotFoundException.class)
public Book findBook(ISBN isbn)
~~~


### 36.4.2 Enabling JSR-107 support

Nothing specific needs to be done to enable the JSR-107 support alongside Spring’s declarative annotation support. Both @EnableCaching and the cache:annotation-driven element will enable automatically the JCache support if both the JSR-107 API and the spring-context-support module are present in the classpath.

Depending of your use case, the choice is basically yours. You can even mix and match services using the JSR-107 API and others using Spring’s own annotations. Be aware however that if these services are impacting the same caches, a consistent and identical key generation implementation should be used.


### 36.4.2 JSR-107 支持可用

没有特定的需要去做去使JSR-107可用支持与Spring的声明式注释的支持.若JSR-107接口和spring-context-support模块呈现于classpath，则@EnableCaching和cache:annotation-driven元素将会自动启用JCache支持.


根据你的使用情况,选择权基于你.你甚至可以混合和匹配服务使用JSR-107接口和其他使用spring自身的注解.注意的是不论折现服务影响相同的缓存,一致且相同的密钥生成器实现应被使用.


36.5 Declarative XML-based caching
If annotations are not an option (no access to the sources or no external code), one can use XML for declarative caching. So instead of annotating the methods for caching, one specifies the target method and the caching directives externally (similar to the declarative transaction management advice). The previous example can be translated into:


### 36.5 基于XML的缓存说明

若注解不是选择(无法访问源码或没有外部代码),则可以使用XML来缓存注解.因此,代替标注方法为缓存,一个指定的目标方法,缓存指令外(类似事物管理指令通知).前面的例子可以翻译为:

![image](http://yangege.b0.upaiyun.com/11e64263e55a8.png)


In the configuration above, the bookService is made cacheable. The caching semantics to apply are encapsulated in the cache:advice definition which instructs method findBooks to be used for putting data into the cache while method loadBooks for evicting data. Both definitions are working against the books cache.

The aop:config definition applies the cache advice to the appropriate points in the program by using the AspectJ pointcut expression (more information is available in Chapter 11, Aspect Oriented Programming with Spring). In the example above, all methods from the BookService are considered and the cache advice applied to them.

The declarative XML caching supports all of the annotation-based model so moving between the two should be fairly easy - further more both can be used inside the same application. The XML based approach does not touch the target code however it is inherently more verbose; when dealing with classes with overloaded methods that are targeted for caching, identifying the proper methods does take an extra effort since the method argument is not a good discriminator - in these cases, the AspectJ pointcut can be used to cherry pick the target methods and apply the appropriate caching functionality. However through XML, it is easier to apply a package/group/interface-wide caching (again due to the AspectJ pointcut) and to create template-like definitions (as we did in the example above by defining the target cache through the cache:definitions cache attribute).


在配置上,bookService是可缓存的.该缓存语义封装在缓存中:通知定义会知道findBooks方法被使用于放置数据进缓存,当loadBooks方法清空数据.这两个定义都是针对书缓存.

AOP:配置定义请求缓存通知在程序中通过使用AspectJ切入点表达式适配该点(第11章获取更多信息,spring的面向切面编程).在上面的例子中,来自BookService的所有方法都被考虑到了,缓存通过请求也给予它们.

声明式的缓存支持所有基于注释的模型,因此,在两者之间移动非常容易,而且,两者都可以在同一个应用中被应用.基于XML的方法不接触目标代码,但它本质上是更冗长的;在处理重载的方法时,有针对性的缓存,识别适当的方法会花费额外的努力,因为方法的参数不是一个好的识别器-在这些情况下,AspectJ切入点可以用来挑选目标方法应用适当的缓存功能.然后通过XML,更容易被请求为一个package/group/interface-wide缓存(再次因为AspectJ切入点),创建一个template-like定义(就像我们在上面的例子中通过cache:definitions cache 属性定义目标缓存)


36.6 Configuring the cache storage
Out of the box, the cache abstraction provides several storage integration. To use them, one needs to simply declare an appropriate CacheManager - an entity that controls and manages Caches and can be used to retrieve these for storage.


36.6 配置缓存存储

开箱即用,缓存抽象提供了不同的存储一体化.为了使用他们,必须简单声明一个合适的CacheManager-控制并管理缓存,可以用于检索这些存储的实体.


36.6.1 JDK ConcurrentMap-based Cache

The JDK-based Cache implementation resides under org.springframework.cache.concurrent package. It allows one to use ConcurrentHashMap as a backing Cache store.

<!-- simple cache manager -->
<bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager">
    <property name="caches">
        <set>
            <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="default"/>
            <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="books"/>
        </set>
    </property>
</bean>

The snippet above uses the SimpleCacheManager to create a CacheManager for the two nested ConcurrentMapCache instances named default and books. Note that the names are configured directly for each cache.

As the cache is created by the application, it is bound to its lifecycle, making it suitable for basic use cases, tests or simple applications. The cache scales well and is very fast but it does not provide any management or persistence capabilities nor eviction contracts.


36.6.1 基于ConcurrentMap的JDK缓存

基于JDK的缓存实现在org.springframework.cache.concurrent下面.允许使用ConcurrentHashMap作为备份缓存存储

~~~
<!-- simple cache manager -->
<bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager">
    <property name="caches">
        <set>
            <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="default"/>
            <bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean" p:name="books"/>
        </set>
    </property>
</bean>
~~~

以上片段,为了叫做default和books的两个内嵌的ConcurrentMapCache实例,使用SimpleCacheManager去创建CacheManager.注意命名对每个缓存都是直接配置的.

当缓存被应用创建的时候,它绑定到它的生命周期,使其适合于基本用例,测试或者简单应用程序.缓存的尺度是好的，快速的,但是它未提供任何管理或持续能力清空合同.



36.6.2 Ehcache-based Cache

[Note]
Ehcache 3.x is fully JSR-107 compliant and no dedicated support is required for it.
The Ehcache 2.x implementation is located under org.springframework.cache.ehcache package. Again, to use it, one simply needs to declare the appropriate CacheManager:

<bean id="cacheManager"
      class="org.springframework.cache.ehcache.EhCacheCacheManager" p:cache-manager-ref="ehcache"/>

<!-- EhCache library setup -->
<bean id="ehcache"
      class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean" p:config-location="ehcache.xml"/>
This setup bootstraps the ehcache library inside Spring IoC (through the ehcache bean) which is then wired into the dedicated CacheManager implementation. Note the entire ehcache-specific configuration is read from ehcache.xml.



### 36.6.2 基于Ehcache的缓存

Ehcache 3.x完美兼容JSR-107,并且不需要特别的支持.
Ehcache2.x 实现在org.springframework.cache.ehcache包下面.去使用它,仅需要声明适当的CacheManager:

~~~
<bean id="cacheManager"
      class="org.springframework.cache.ehcache.EhCacheCacheManager" p:cache-manager-ref="ehcache"/>
<!-- EhCache library setup -->
<bean id="ehcache"
      class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean" p:config-location="ehcache.xml"/>      
~~~

设置引导在SpringIOC(通过ehcache bean)内的ehcache库,然后连接到专用的CacheManager实现上.注意,所有的ehcache-specific配置都是从ehcache.xml中来.



36.6.3 Caffeine Cache

Caffeine is a Java 8 rewrite of Guava’s cache and its implementation is located under org.springframework.cache.caffeine package and provides access to several features of Caffeine.

Configuring a CacheManager that creates the cache on demand is straightforward:

<bean id="cacheManager"
      class="org.springframework.cache.caffeine.CaffeineCacheManager"/>
It is also possible to provide the caches to use explicitly. In that case, only those will be made available by the manager:

<bean id="cacheManager" class="org.springframework.cache.caffeine.CaffeineCacheManager">
    <property name="caches">
        <set>
            <value>default</value>
            <value>books</value>
        </set>
    </property>
</bean>
The Caffeine CacheManager also supports customs Caffeine and CacheLoader. See the Caffeine documentation for more information about those.


### 36.6.3 咖啡因Cache

Caffeine是在java8中重写Guava's cache的缓存，被放置于org.springframework.cache.caffeine包下面,提供几种Caffeine的特性.

配置CacheManager是创建这种缓存最直截了当的

~~~
<bean id="cacheManager"
      class="org.springframework.cache.caffeine.CaffeineCacheManager"/>
~~~

它也可以提供使用明确的缓存.在那种情况下,只有通过以下缓存才将会起作用:

~~~
<bean id="cacheManager" class="org.springframework.cache.caffeine.CaffeineCacheManager">
    <property name="caches">
        <set>
            <value>default</value>
            <value>books</value>
        </set>
    </property>
</bean>
~~~

Caffeine CacheManger同意支持自定义Caffeine和CacheLoader.参阅Caffeine文档获取更多这方面的信息


36.6.4 Guava Cache

The Guava implementation is located under org.springframework.cache.guava package and provides access to several features of Guava.

Configuring a CacheManager that creates the cache on demand is straightforward:

<bean id="cacheManager"
      class="org.springframework.cache.guava.GuavaCacheManager"/>
It is also possible to provide the caches to use explicitly. In that case, only those will be made available by the manager:

<bean id="cacheManager" class="org.springframework.cache.guava.GuavaCacheManager">
    <property name="caches">
        <set>
            <value>default</value>
            <value>books</value>
        </set>
    </property>
</bean>
The Guava CacheManager also supports customs CacheBuilder and CacheLoader. See the Guava documentation for more information about those.


### 36.6.4 番石榴Cache

Guava实现被放置于org.springframework.cache.guava包下面,提供了Guava的一些特性.

配置CacheManager是创建这种缓存最直截了当的

~~~
<bean id="cacheManager"
      class="org.springframework.cache.guava.GuavaCacheManager"/>
~~~

它也可以提供使用明确的缓存.在那种情况下,只有通过以下缓存才将会起作用:

~~~
<bean id="cacheManager" class="org.springframework.cache.guava.GuavaCacheManager">
    <property name="caches">
        <set>
            <value>default</value>
            <value>books</value>
        </set>
    </property>
</bean>
~~~

Guava CacheManager同样支持自定义CacheBuilder和CacheLoader.参阅Guava文档获取更多这方面的信息.


36.6.5 GemFire-based Cache

GemFire is a memory-oriented/disk-backed, elastically scalable, continuously available, active (with built-in pattern-based subscription notifications), globally replicated database and provides fully-featured edge caching. For further information on how to use GemFire as a CacheManager (and more), please refer to the Spring Data GemFire reference documentation.


### 36.6.5 GemFire-based Cache

GemFire是一个面向内存/磁盘备份,弹性可伸缩,平均连续,活跃的(内置基于通知订阅),全局复制数据库,提供全功能的边缘缓存.
进一步的信息去使用GemFire作为一个CaccheManage(及以上)，请参阅Spring数据GemFire参考文档.


36.6.6 JSR-107 Cache

JSR-107 compliant caches can also be used by Spring’s caching abstraction. The JCache implementation is located under org.springframework.cache.jcache package.

Again, to use it, one simply needs to declare the appropriate CacheManager:

<bean id="cacheManager"
      class="org.springframework.cache.jcache.JCacheCacheManager"
      p:cache-manager-ref="jCacheManager"/>

<!-- JSR-107 cache manager setup  -->
<bean id="jCacheManager" .../>

### 36.6.6 JSR-107 Cache

JSR-107兼容缓存,同样能够被Spring缓存抽象所使用.JCache实现被放置于org.springframework.cache.jcache包下.

同样的,使用它,仅需要声明适当的CacheManger：

~~~
<bean id="cacheManager"
      class="org.springframework.cache.jcache.JCacheCacheManager"
      p:cache-manager-ref="jCacheManager"/>

<!-- JSR-107 cache manager setup  -->
<bean id="jCacheManager" .../>
~~~



36.6.7 Dealing with caches without a backing store

Sometimes when switching environments or doing testing, one might have cache declarations without an actual backing cache configured. As this is an invalid configuration, at runtime an exception will be thrown since the caching infrastructure is unable to find a suitable store. In situations like this, rather then removing the cache declarations (which can prove tedious), one can wire in a simple, dummy cache that performs no caching - that is, forces the cached methods to be executed every time:

<bean id="cacheManager" class="org.springframework.cache.support.CompositeCacheManager">
    <property name="cacheManagers">
        <list>
            <ref bean="jdkCache"/>
            <ref bean="gemfireCache"/>
        </list>
    </property>
    <property name="fallbackToNoOpCache" value="true"/>
</bean>
The CompositeCacheManager above chains multiple CacheManagers and additionally, through the fallbackToNoOpCache flag, adds a no op cache that for all the definitions not handled by the configured cache managers. That is, every cache definition not found in either jdkCache or gemfireCache (configured above) will be handled by the no op cache, which will not store any information causing the target method to be executed every time.


36.6.7 处理没有存储器的缓存

有时候切环境或者做测试时,可能没有实际缓存配置的缓存声明.因为这是一种不合法的配置,由于缓存设施不可能找到合适的存储,运行时将会抛出异常.如同该种情况,而不是移除缓存声明(这可以证明乏味).可以有一种简单的传递,虚拟缓存执行没有缓存-也就是说,每次强制执行缓存的方法:

~~~
<bean id="cacheManager" class="org.springframework.cache.support.CompositeCacheManager">
    <property name="cacheManagers">
        <list>
            <ref bean="jdkCache"/>
            <ref bean="gemfireCache"/>
        </list>
    </property>
    <property name="fallbackToNoOpCache" value="true"/>
</bean>
~~~

以上的CompositeCacheManager链上多个CacheManager和条件,通过fallbackToNoOpCache标志,为所有的定义添加一个无操作的缓存,不需要通过缓存管理器处理.也就是说,每一个缓存定义不会在jdkCache或者gemfireCache(如上配置)发现,将会通过无操作缓存来处理,每次目标方法被执行,将不会存储任何信息.


36.7 Plugging-in different back-end caches
Clearly there are plenty of caching products out there that can be used as a backing store. To plug them in, one needs to provide a CacheManager and Cache implementation since unfortunately there is no available standard that we can use instead. This may sound harder than it is since in practice, the classes tend to be simple adapters that map the caching abstraction framework on top of the storage API as the ehcache classes can show. Most CacheManager classes can use the classes in org.springframework.cache.support package, such as AbstractCacheManager which takes care of the boiler-plate code leaving only the actual mapping to be completed. We hope that in time, the libraries that provide integration with Spring can fill in this small configuration gap.



### 36.7 不同后端缓存插入

显然，市面上有大量的缓存产品,都可以作为存储器使用.使用它们,仅需提供一个CacheManage和缓存实现,然而,不幸的时候还未曾有一个可用的标准让我们可以代替使用.这在实际应用中听起来更难,类趋向于简单的适配,匹配缓存抽象框架在存储的API上如同ehcache类能够展示.大多数CacheManager类能够使用org.springframework.cache.support包下的类,例如AbstractCacheManage负责样板化文件,只留下实际的映射需要完善.我们希望即使的通过spring提供的集成的类库能够填充这个小的配置缺口.


36.8 How can I set the TTL/TTI/Eviction policy/XXX feature?
Directly through your cache provider. The cache abstraction is…​ well, an abstraction not a cache implementation. The solution you are using might support various data policies and different topologies which other solutions do not (take for example the JDK ConcurrentHashMap) - exposing that in the cache abstraction would be useless simply because there would no backing support. Such functionality should be controlled directly through the backing cache, when configuring it or through its native API.


### 36.8 我如何设置TTL/TTI/清空策略/XXX特性

直接通过你的缓存提供者.缓存抽象是一个..好吧,是一个抽象而非一个缓存实现.你使用的方案可能支持多种数据策略,不同的拓扑,其他方案(比如JDK的ConcurrentHashMap)-在缓存抽象中被遗弃将会只是无用的,因为这些没有后续支撑.当配置该缓存时,或通过其原生API时，这些功能应当直接通过后台缓存来控制。



