package de.hska.lkit.demo.redis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;


@Configuration
public class RedisConfiguration {
	@Bean
	public JedisConnectionFactory getConnectionFactory() {
		// falls andere als die Default Werte gesetzt werden sollen
//		JedisConnectionFactory jRedisConnectionFactory = new JedisConnectionFactory(new JedisPoolConfig());
//		jRedisConnectionFactory.setHostName("localhost");
//		jRedisConnectionFactory.setPort(6379);
//		jRedisConnectionFactory.setPassword("");
//		return jRedisConnectionFactory;
		System.out.println("DB Connection");
		return new JedisConnectionFactory();
	}

	@Bean(name = "stringRedisTemplate")
	public StringRedisTemplate getStringRedisTemplate() {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(getConnectionFactory());
		stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
		stringRedisTemplate.setHashValueSerializer(new StringRedisSerializer());
		stringRedisTemplate.setValueSerializer(new StringRedisSerializer());
		System.out.println("String Template");
		return stringRedisTemplate;
	}

	
	@Bean(name = "redisTemplate")
	public RedisTemplate<String, Object> getRedisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(getConnectionFactory());
		return redisTemplate;
	}

}