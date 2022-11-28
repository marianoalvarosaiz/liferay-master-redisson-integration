# Solution Based on Extending Redisson Session Manager

Extends `RedissonSessionManager` to use Liferay Serializer/Deserializer. The idea is to save the context name of the object implementation module classloader during the serialization process. Afterwards, during the deserialization the classloader can be retrieved from the ClassLoaderPool using that saved context name.

## How to Configure:
How to configure it:

   1. Change the configuration in `$TOMCAT_HOME/conf/Catalina/localhost/ROOT.xml` to use LiferayDelegatedRedissonSessionManager:
   ```
   <Manager className="com.liferay.redis.redisson.integration.tomcat.LiferayDelegatedRedissonSessionManager"
  configPath="${catalina.base}/redisson.conf"
  readMode="REDIS" updateMode="DEFAULT" broadcastSessionEvents="false" broadcastSessionUpdates="false"/>
  ```

   2. Configure Redisson with Redis address in `$CATALINA_BASE/redisson.conf`
   ```
   {
       "singleServerConfig":{
           "address": "redis://127.0.0.1:6379"
       },
       "threads":0,
       "nettyThreads":0,
       "transportMode":"NIO"
   }
   ```
   
   3. Deploy `com.liferay.redis.redisson.integration.jar` to `$TOMCAT_HOME/webapps/ROOT/WEB-INF/shielded-container-lib`.
   
   4. Deploy `com.liferay.redis.redisson.integration.tomcat.jar` to `$TOMCAT_HOME/lib`.

