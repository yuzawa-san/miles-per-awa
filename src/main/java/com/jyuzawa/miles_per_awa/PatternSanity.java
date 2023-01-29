package com.jyuzawa.miles_per_awa;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpCookie;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PatternSanity {

	public static void main(String[] args) {
		Map<String,Object> attributes = new ConcurrentHashMap<>();
		RequestPredicates.path("/foo/bar").test(new ServerRequest() {

			@Override
			public String methodName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public URI uri() {
				// TODO Auto-generated method stub
				return null;
			}
			
			 @Override
			public RequestPath requestPath() {
				return RequestPath.parse("/foo/bar", "");
			}

			@Override
			public UriBuilder uriBuilder() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Headers headers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public MultiValueMap<String, HttpCookie> cookies() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Optional<InetSocketAddress> remoteAddress() {
				// TODO Auto-generated method stub
				return Optional.empty();
			}

			@Override
			public Optional<InetSocketAddress> localAddress() {
				// TODO Auto-generated method stub
				return Optional.empty();
			}

			@Override
			public List<HttpMessageReader<?>> messageReaders() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> T body(BodyExtractor<T, ? super ServerHttpRequest> extractor) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> T body(BodyExtractor<T, ? super ServerHttpRequest> extractor, Map<String, Object> hints) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Mono<T> bodyToMono(Class<? extends T> elementClass) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> typeReference) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Flux<T> bodyToFlux(Class<? extends T> elementClass) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> Flux<T> bodyToFlux(ParameterizedTypeReference<T> typeReference) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Map<String, Object> attributes() {
				return attributes;
			}

			@Override
			public MultiValueMap<String, String> queryParams() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Map<String, String> pathVariables() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Mono<WebSession> session() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Mono<? extends Principal> principal() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Mono<MultiValueMap<String, String>> formData() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Mono<MultiValueMap<String, Part>> multipartData() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ServerWebExchange exchange() {
				// TODO Auto-generated method stub
				return null;
			}
			
		});
	}

}
