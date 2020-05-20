### 标签
``````
<!-- 本地仓库的路径。默认值为 -->
    <localRepository>/opt/repository</localRepository>

<!--Maven是否需要和用户交互以获得输入。默认为true(可以交互)。-->
    <interactiveMode>true</interactiveMode>

<!--Maven是否需要使用plugin-registry.xml文件来管理插件版本。如果需要让Maven使用文件来管理插件版本，则设为true。默认为false。-->
    <usePluginRegistry>false</usePluginRegistry>

<!--表示Maven是否需要在离线模式下运行。如果构建系统需要在离线模式下运行，则为true，默认为false。当由于网络设置原因或者安全因素，构建服务器不能连接远程仓库的时候，该配置就十分有用。 -->
    <offline>false</offline>

<!--当插件的组织Id（groupId）没有显式提供时，供搜寻插件组织Id（groupId）的列表。该元素包含一个pluginGroup元素列表，每个子元素包含了一个组织Id（groupId）。
当我们使用某个插件，并且没有在命令行为其提供组织Id（groupId）的时候，Maven就会使用该列表。默认情况下该列表包含了org.apache.maven.plugins和org.codehaus.mojo -->
    <pluginGroups>
      <pluginGroup>org.apache.maven.plugins</pluginGroup>
      <pluginGroup>org.codehaus.mojo</pluginGroup>
    </pluginGroups>

<!--配置服务端的一些设置。一些设置如安全证书不应该和pom.xml一起分发。这种类型的信息应该存在于构建服务器上的settings.xml文件中。-->
    <servers>
      <!--服务器元素包含配置服务器时需要的信息 -->
      <server>
       <id>server001</id>
       <username>my_login</username>
       <password>my_password</password>
       <!--鉴权时使用的私钥位置。和前两个元素类似，私钥位置和私钥密码指定了一个私钥的路径（默认是${user.home}/.ssh/id_dsa）以及如果需要的话，一个密语。将来passphrase和password元素可能会被提取到外部，但目前它们必须在settings.xml文件以纯文本的形式声明。 -->
       <privateKey>${usr.home}/.ssh/id_dsa</privateKey>
       <!--鉴权时使用的私钥密码。-->
       <passphrase>some_passphrase</passphrase>
       <filePermissions>664</filePermissions>
       <directoryPermissions>775</directoryPermissions>
      </server>
    </servers>

<!--远程仓库列表，它是Maven用来填充构建系统本地仓库所使用的一组远程项目。 -->
    <repositories>
      <repository>
         <id>codehausSnapshots</id>
         <name>Codehaus Snapshots</name>
         <releases>
            <enabled>false</enabled>
            <updatePolicy>always</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
         </releases>
         <snapshots>
            <enabled/>true<checksumPolicy/>
         </snapshots>
         <url>http://snapshots.maven.codehaus.org/maven2</url>
         <layout>default</layout>
      </repository>
    </repositories>

<!--为仓库列表配置的下载镜像列表。我们可以在pom中定义一个下载工件的时候所使用的远程仓库。
但是有时候这个远程仓库会比较忙，所以这个时候人们就想着给它创建镜像以缓解远程仓库的压力，也就是说会把对远程仓库的请求转换到对其镜像地址的请求 -->
    <mirrors>
      <!--给定仓库的下载镜像。 -->
      <mirror>
       <id>planetmirror.com</id>
       <name>PlanetMirror Australia</name>
       <url>http://downloads.planetmirror.com/pub/maven2</url>
       <!--被镜像的服务器的id。例如，如果我们要设置了一个Maven中央仓库（http://repo.maven.apache.org/maven2/）的镜像，就需要将该元素设置成central。这必须和中央仓库的id central完全一致。-->
       <mirrorOf>central</mirrorOf>
      </mirror>
    </mirrors>

<!--自动触发profile的条件逻辑。activation元素并不是激活profile的唯一方式。settings.xml文件中的activeProfile元素可以包含profile的id。profile也可以通过在命令行，使用-P标记和逗号分隔的列表来显式的激活（如，-P test）。-->
    <activation>
    <!--profile默认是否激活的标识-->
        <activeByDefault>false</activeByDefault>
        <jdk>1.5</jdk>
        <os>
          <name>Windows XP</name>
          <family>Windows</family>
          <arch>x86</arch>
          <version>5.1.2600</version>
        </os>
    <!--如果Maven检测到某一个属性（其值可以在POM中通过${name}引用），其拥有对应的name = 值，Profile就会被激活。如果值字段是空的，那么存在属性名称字段就会激活profile，否则按区分大小写方式匹配属性值字段-->
        <property>
           <name>mavenVersion</name>
           <value>2.0.3</value>
        </property>
    <!--提供一个文件名，通过检测该文件的存在或不存在来激活profile。missing检查文件是否存在，如果不存在则激活profile。另一方面，exists则会检查文件是否存在，如果存在则激活profile。-->
        <file>
           <exists>${basedir}/file2.properties</exists>
           <missing>${basedir}/file1.properties</missing>
        </file>
     </activation>

<!--手动激活profiles的列表，按照profile被应用的顺序定义activeProfile。 该元素包含了一组activeProfile元素，每个activeProfile都含有一个profile id。-->
     <activeProfiles>
        <activeProfile>env-test</activeProfile>
     </activeProfiles>

<!--根据环境参数来调整构建配置的列表。 -->
     <profiles> 
        <profile>
            <id>jdk1.8</id>
            <activation>
                <activeByDefault>true</activeByDefault>
                <jdk>1.8</jdk>
            </activation>
            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <maven.compiler.source>1.8</maven.compiler.source>
                <maven.compiler.target>1.8</maven.compiler.target>
                <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>
            </properties>
        </profile>
     </profiles>
``````