# Solution Based on Extending Redisson Session Manager

Extends `RedissonSessionManager` to use Liferay Serializer/Deserializer. The idea is to save the context name of the object implementation module classloader during the serialization process. Afterwards, during the deserialization the classloader can be retrieved from the ClassLoaderPool using that saved context name.

