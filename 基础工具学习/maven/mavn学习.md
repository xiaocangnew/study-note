##### Maven本质上是一个插件框架，它的核心并不执行任何具体的构建任务，所有这些任务都交给插件来完成。

### 1插件类型
1.构建插件	在生成过程中执行，并在 pom.xml 中的<build/> 元素进行配置
2.报告插件	在网站生成期间执行，在 pom.xml 中的 <reporting/> 元素进行配置

### 2常见的插件：
第一个列表的GroupId为org.apache.maven.plugins，这里的插件最为成熟，具体地址为：http://maven.apache.org/plugins/index.html。
第二个列表的GroupId为org.codehaus.mojo，这里的插件没有那么核心，但也有不少十分有用，其地址为：http://mojo.codehaus.org/plugins.html。

### 3插件使用
用户可以通过两种方式调用Maven插件目标。
1.是将插件目标与生命周期阶段（lifecycle phase）绑定，这样用户在命令行只是输入生命周期阶段而已。
例如Maven默认将maven-compiler-plugin的compile目标与compile生命周期阶段绑定，因此命令mvn compile实际上是先定位到compile这一生命周期阶段，然后再根据绑定关系调用maven-compiler-plugin的compile目标。
2.是直接在命令行指定要执行的插件目标。
例如:mvn archetype:generate 就表示调用maven-archetype-plugin的generate目标，这种带冒号的调用方式与生命周期无关。

### 4依赖继承
1.如果父项目中在<dependencies> 中声明，即使在子项目中不写该依赖项，那么子项目仍然会从父项目中继承该依赖项（全部继承）
2.<dependencyManagement>里只是声明依赖，并不实现引入，因此子项目需要显示的声明需要用的依赖。
如果不在子项目中声明依赖，是不会从父项目中继承下来的；只有在子项目中写了该依赖项，并且没有指定具体版本，才会从父项目中继承该项，
并且version和scope都读取自父pom;另外如果子项目中指定了版本号，那么会使用子项目中指定的jar版本。

### 5标签
## 1.scope
在POM 4中，<dependency>中还引入了<scope>，它主要管理依赖的部署。目前<scope>可以使用5个值： 
* compile，缺省值，适用于所有阶段，会随着项目一起发布。 
* provided，类似compile，只在编译时用这个依赖，不会打包进war里， 如java自带jdk等； 
* runtime，只在运行时使用，如JDBC驱动，适用运行和测试阶段。 
* test，只在测试时使用，用于编译和运行测试代码。不会随项目发布。 
* system，类似provided，需要显式提供包含依赖的jar，Maven不会在Repository中查找它

## 2.phase, goal 标签
不同类型的plugins有不同的默认phase。
在每个阶段（phase）中，又分别有小的goals，查看goals命令为：mvn help:describe -Dplugin=groupId:artifactId:version。(这里只能看公有插件的goal，自己项目是没有得)
如果要将goals插入phase中，需要用<executions>标签。

## 3. packaging 元素，是属于module级别
<packaging>pom</packaging>   --------->   父类型都为pom类型
<packaging>jar</packaging>      --------->   内部调用或者是作服务使用， 默认类型
<packaging>war</packaging>    --------->   需要部署的项目

###4. parent标签
指明该项目的父项目，在父项目中，依然可以指定  
<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>zcs.spring-boot-starter-parent</artifactId>
		<version>1.5.2.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
</parent>  
这个说明父项目使用的依赖；


## 3.build标签，
```
 <build>
    <resources>  资源往往不是代码，而是properties或xml文件，无需编译，构建过程中往往会将资源文件从源路径复制到指定的目标路径，resources则给出各个资源在maven项目中的具体路径
      <resouce>
        <targetPath>资源文件的目标路径 </>
        <filtering>构建过程中是否对资源进行过滤，默认false</>
        <directory>资源文件源路径，默认位于${basedir}/src/main/resources/目录下</>
        <includes>
            <include>一组文件名的匹配模式，被匹配的资源文件将被构建过程处理</>
        </includes>
        <excludes>
        </excludes>
      </resource>
    <resouces>
    
    <plugins> 设置构建过程中需要的插件
      <plugin>
         <extensions>是否加载该插件的扩展，默认false </>
         <inherited> 该插件的configuration中的配置是否可以被继承（继承该pom中的其他maven项目），默认true </>
         <configuration> 该插件所需要的特殊配置，在父子项目之间可以覆盖或合</>
         <dependencies>  该插件所需要的依赖类库
             <dependency>
                <id> </>
                <goal> </>
                <phase> </>
             </dependency>
         </dependencies>
         <executions>
         </executions>
      
      </plugin>
    </plugins>
 </build>
 ```

### 常用命令
1. mvn clean install/pacakge  在进入项目的文件后，执行该命令，会把文件下的所有子文件夹都执行一遍
2. mvn clean install -pl 子文件夹名称 -am。  打包指定文件夹( -pl project -am also-make解决依赖)