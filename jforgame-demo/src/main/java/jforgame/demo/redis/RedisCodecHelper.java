package jforgame.demo.redis;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 */
public class RedisCodecHelper {

	private static IRedisSerializer serializer = new ProtobufRedisSerializer();

	/***
	 * @param o 必须有字段加上protobuf注解
	 * @return
	 */
	public static String serialize(Object o) {
		byte[] bytes = Base64.getEncoder().encode(serializer.serialize(o));
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static List<String> serialize(List<Object> objectList) {
		return objectList.stream().map(RedisCodecHelper::serialize).collect(Collectors.toList());
	}

	/**
	 * @param text
	 * @param cls 必须有字段加上protobuf注解标记
	 * @return
	 */
	public static <T> T deserialize(String text, Class<T> cls) {
		return serializer.deserialize(Base64.getDecoder().decode(text), cls);
	}

	public static <T> List<T> deserialize(List<String> texts, Class<T> cls) {
		return texts.stream().map(text -> deserialize(text, cls)).collect(Collectors.toList());
	}

}