/**
 * Created by antonrybochkin on 12.10.16.
 */
public class TestData {
	public static final String csvWithErrors = "#{\n" +
			"#  \"errors\" : [ {\n" +
			"#    \"state\" : \"07\",\n" +
			"#    \"exception\" : [ {\n" +
			"#      \"methodName\" : \"getMetricByName\",\n" +
			"#      \"fileName\" : \"DictionaryServiceImpl.java\",\n" +
			"#      \"lineNumber\" : 199,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.DictionaryServiceImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : null,\n" +
			"#      \"lineNumber\" : -1,\n" +
			"#      \"className\" : \"sun.reflect.GeneratedMethodAccessor172\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"DelegatingMethodAccessorImpl.java\",\n" +
			"#      \"lineNumber\" : 43,\n" +
			"#      \"className\" : \"sun.reflect.DelegatingMethodAccessorImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"Method.java\",\n" +
			"#      \"lineNumber\" : 606,\n" +
			"#      \"className\" : \"java.lang.reflect.Method\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invokeJoinpointUsingReflection\",\n" +
			"#      \"fileName\" : \"AopUtils.java\",\n" +
			"#      \"lineNumber\" : 317,\n" +
			"#      \"className\" : \"org.springframework.aop.support.AopUtils\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invokeJoinpoint\",\n" +
			"#      \"fileName\" : \"ReflectiveMethodInvocation.java\",\n" +
			"#      \"lineNumber\" : 183,\n" +
			"#      \"className\" : \"org.springframework.aop.framework.ReflectiveMethodInvocation\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"proceed\",\n" +
			"#      \"fileName\" : \"ReflectiveMethodInvocation.java\",\n" +
			"#      \"lineNumber\" : 150,\n" +
			"#      \"className\" : \"org.springframework.aop.framework.ReflectiveMethodInvocation\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"CacheInterceptor.java\",\n" +
			"#      \"lineNumber\" : 58,\n" +
			"#      \"className\" : \"org.springframework.cache.interceptor.CacheInterceptor$1\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"execute\",\n" +
			"#      \"fileName\" : \"CacheAspectSupport.java\",\n" +
			"#      \"lineNumber\" : 211,\n" +
			"#      \"className\" : \"org.springframework.cache.interceptor.CacheAspectSupport\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"CacheInterceptor.java\",\n" +
			"#      \"lineNumber\" : 66,\n" +
			"#      \"className\" : \"org.springframework.cache.interceptor.CacheInterceptor\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"proceed\",\n" +
			"#      \"fileName\" : \"ReflectiveMethodInvocation.java\",\n" +
			"#      \"lineNumber\" : 172,\n" +
			"#      \"className\" : \"org.springframework.aop.framework.ReflectiveMethodInvocation\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"JdkDynamicAopProxy.java\",\n" +
			"#      \"lineNumber\" : 204,\n" +
			"#      \"className\" : \"org.springframework.aop.framework.JdkDynamicAopProxy\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"getMetricByName\",\n" +
			"#      \"fileName\" : null,\n" +
			"#      \"lineNumber\" : -1,\n" +
			"#      \"className\" : \"com.sun.proxy.$Proxy53\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"composeRegistry\",\n" +
			"#      \"fileName\" : \"SqlMetaServiceImpl.java\",\n" +
			"#      \"lineNumber\" : 34,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.sql.SqlMetaServiceImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"analyze\",\n" +
			"#      \"fileName\" : \"SqlQueryAnalyzerImpl.java\",\n" +
			"#      \"lineNumber\" : 72,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.sql.SqlQueryAnalyzerImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"executeQueryInternal\",\n" +
			"#      \"fileName\" : \"SqlQueryServiceImpl.java\",\n" +
			"#      \"lineNumber\" : 126,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.sql.SqlQueryServiceImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"executeQuery\",\n" +
			"#      \"fileName\" : \"SqlQueryServiceImpl.java\",\n" +
			"#      \"lineNumber\" : 98,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.sql.SqlQueryServiceImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"execute\",\n" +
			"#      \"fileName\" : \"ApiSqlController.java\",\n" +
			"#      \"lineNumber\" : 80,\n" +
			"#      \"className\" : \"com.axibase.tsd.web.api.ApiSqlController\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"process\",\n" +
			"#      \"fileName\" : \"ApiSqlController.java\",\n" +
			"#      \"lineNumber\" : 58,\n" +
			"#      \"className\" : \"com.axibase.tsd.web.api.ApiSqlController\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"queryCsv\",\n" +
			"#      \"fileName\" : \"ApiSqlController.java\",\n" +
			"#      \"lineNumber\" : 42,\n" +
			"#      \"className\" : \"com.axibase.tsd.web.api.ApiSqlController\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke0\",\n" +
			"#      \"fileName\" : \"NativeMethodAccessorImpl.java\",\n" +
			"#      \"lineNumber\" : -2,\n" +
			"#      \"className\" : \"sun.reflect.NativeMethodAccessorImpl\",\n" +
			"#      \"nativeMethod\" : true\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"NativeMethodAccessorImpl.java\",\n" +
			"#      \"lineNumber\" : 57,\n" +
			"#      \"className\" : \"sun.reflect.NativeMethodAccessorImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"DelegatingMethodAccessorImpl.java\",\n" +
			"#      \"lineNumber\" : 43,\n" +
			"#      \"className\" : \"sun.reflect.DelegatingMethodAccessorImpl\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"Method.java\",\n" +
			"#      \"lineNumber\" : 606,\n" +
			"#      \"className\" : \"java.lang.reflect.Method\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"InvocableHandlerMethod.java\",\n" +
			"#      \"lineNumber\" : 215,\n" +
			"#      \"className\" : \"org.springframework.web.method.support.InvocableHandlerMethod\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invokeForRequest\",\n" +
			"#      \"fileName\" : \"InvocableHandlerMethod.java\",\n" +
			"#      \"lineNumber\" : 132,\n" +
			"#      \"className\" : \"org.springframework.web.method.support.InvocableHandlerMethod\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invokeAndHandle\",\n" +
			"#      \"fileName\" : \"ServletInvocableHandlerMethod.java\",\n" +
			"#      \"lineNumber\" : 104,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invokeHandleMethod\",\n" +
			"#      \"fileName\" : \"RequestMappingHandlerAdapter.java\",\n" +
			"#      \"lineNumber\" : 743,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handleInternal\",\n" +
			"#      \"fileName\" : \"RequestMappingHandlerAdapter.java\",\n" +
			"#      \"lineNumber\" : 672,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"AbstractHandlerMethodAdapter.java\",\n" +
			"#      \"lineNumber\" : 82,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doDispatch\",\n" +
			"#      \"fileName\" : \"DispatcherServlet.java\",\n" +
			"#      \"lineNumber\" : 933,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.DispatcherServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doService\",\n" +
			"#      \"fileName\" : \"DispatcherServlet.java\",\n" +
			"#      \"lineNumber\" : 867,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.DispatcherServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"processRequest\",\n" +
			"#      \"fileName\" : \"FrameworkServlet.java\",\n" +
			"#      \"lineNumber\" : 953,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.FrameworkServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doGet\",\n" +
			"#      \"fileName\" : \"FrameworkServlet.java\",\n" +
			"#      \"lineNumber\" : 844,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.FrameworkServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"service\",\n" +
			"#      \"fileName\" : \"HttpServlet.java\",\n" +
			"#      \"lineNumber\" : 735,\n" +
			"#      \"className\" : \"javax.servlet.http.HttpServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"service\",\n" +
			"#      \"fileName\" : \"FrameworkServlet.java\",\n" +
			"#      \"lineNumber\" : 829,\n" +
			"#      \"className\" : \"org.springframework.web.servlet.FrameworkServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"service\",\n" +
			"#      \"fileName\" : \"HttpServlet.java\",\n" +
			"#      \"lineNumber\" : 848,\n" +
			"#      \"className\" : \"javax.servlet.http.HttpServlet\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"ServletHolder.java\",\n" +
			"#      \"lineNumber\" : 684,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHolder\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 1496,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler$CachedChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilterWithMonitoring\",\n" +
			"#      \"fileName\" : \"SimonServletFilterFixed.java\",\n" +
			"#      \"lineNumber\" : 229,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.monitoring.SimonServletFilterFixed\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"SimonServletFilterFixed.java\",\n" +
			"#      \"lineNumber\" : 214,\n" +
			"#      \"className\" : \"com.axibase.tsd.service.monitoring.SimonServletFilterFixed\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 1467,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler$CachedChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 330,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invoke\",\n" +
			"#      \"fileName\" : \"FilterSecurityInterceptor.java\",\n" +
			"#      \"lineNumber\" : 118,\n" +
			"#      \"className\" : \"org.springframework.security.web.access.intercept.FilterSecurityInterceptor\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterSecurityInterceptor.java\",\n" +
			"#      \"lineNumber\" : 84,\n" +
			"#      \"className\" : \"org.springframework.security.web.access.intercept.FilterSecurityInterceptor\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"ExceptionTranslationFilter.java\",\n" +
			"#      \"lineNumber\" : 113,\n" +
			"#      \"className\" : \"org.springframework.security.web.access.ExceptionTranslationFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"SessionManagementFilter.java\",\n" +
			"#      \"lineNumber\" : 103,\n" +
			"#      \"className\" : \"org.springframework.security.web.session.SessionManagementFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"AnonymousAuthenticationFilter.java\",\n" +
			"#      \"lineNumber\" : 113,\n" +
			"#      \"className\" : \"org.springframework.security.web.authentication.AnonymousAuthenticationFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"RememberMeAuthenticationFilter.java\",\n" +
			"#      \"lineNumber\" : 146,\n" +
			"#      \"className\" : \"org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"SecurityContextHolderAwareRequestFilter.java\",\n" +
			"#      \"lineNumber\" : 154,\n" +
			"#      \"className\" : \"org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"RequestCacheAwareFilter.java\",\n" +
			"#      \"lineNumber\" : 45,\n" +
			"#      \"className\" : \"org.springframework.security.web.savedrequest.RequestCacheAwareFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"BasicAuthenticationFilter.java\",\n" +
			"#      \"lineNumber\" : 201,\n" +
			"#      \"className\" : \"org.springframework.security.web.authentication.www.BasicAuthenticationFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"AbstractAuthenticationProcessingFilter.java\",\n" +
			"#      \"lineNumber\" : 199,\n" +
			"#      \"className\" : \"org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"LogoutFilter.java\",\n" +
			"#      \"lineNumber\" : 110,\n" +
			"#      \"className\" : \"org.springframework.security.web.authentication.logout.LogoutFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilterInternal\",\n" +
			"#      \"fileName\" : \"WebAsyncManagerIntegrationFilter.java\",\n" +
			"#      \"lineNumber\" : 50,\n" +
			"#      \"className\" : \"org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"OncePerRequestFilter.java\",\n" +
			"#      \"lineNumber\" : 106,\n" +
			"#      \"className\" : \"org.springframework.web.filter.OncePerRequestFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"SecurityContextPersistenceFilter.java\",\n" +
			"#      \"lineNumber\" : 87,\n" +
			"#      \"className\" : \"org.springframework.security.web.context.SecurityContextPersistenceFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 342,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy$VirtualFilterChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilterInternal\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 192,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"FilterChainProxy.java\",\n" +
			"#      \"lineNumber\" : 160,\n" +
			"#      \"className\" : \"org.springframework.security.web.FilterChainProxy\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"invokeDelegate\",\n" +
			"#      \"fileName\" : \"DelegatingFilterProxy.java\",\n" +
			"#      \"lineNumber\" : 343,\n" +
			"#      \"className\" : \"org.springframework.web.filter.DelegatingFilterProxy\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"DelegatingFilterProxy.java\",\n" +
			"#      \"lineNumber\" : 260,\n" +
			"#      \"className\" : \"org.springframework.web.filter.DelegatingFilterProxy\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 1467,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler$CachedChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilterInternal\",\n" +
			"#      \"fileName\" : \"CharacterEncodingFilter.java\",\n" +
			"#      \"lineNumber\" : 88,\n" +
			"#      \"className\" : \"org.springframework.web.filter.CharacterEncodingFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"OncePerRequestFilter.java\",\n" +
			"#      \"lineNumber\" : 106,\n" +
			"#      \"className\" : \"org.springframework.web.filter.OncePerRequestFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 1467,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler$CachedChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"UserAgentFilter.java\",\n" +
			"#      \"lineNumber\" : 82,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlets.UserAgentFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"GzipFilter.java\",\n" +
			"#      \"lineNumber\" : 294,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlets.GzipFilter\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doFilter\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 1467,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler$CachedChain\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doHandle\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 501,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doHandle\",\n" +
			"#      \"fileName\" : \"SessionHandler.java\",\n" +
			"#      \"lineNumber\" : 229,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.session.SessionHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doHandle\",\n" +
			"#      \"fileName\" : \"ContextHandler.java\",\n" +
			"#      \"lineNumber\" : 1086,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.handler.ContextHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doScope\",\n" +
			"#      \"fileName\" : \"ServletHandler.java\",\n" +
			"#      \"lineNumber\" : 429,\n" +
			"#      \"className\" : \"org.eclipse.jetty.servlet.ServletHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doScope\",\n" +
			"#      \"fileName\" : \"SessionHandler.java\",\n" +
			"#      \"lineNumber\" : 193,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.session.SessionHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"doScope\",\n" +
			"#      \"fileName\" : \"ContextHandler.java\",\n" +
			"#      \"lineNumber\" : 1020,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.handler.ContextHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"ScopedHandler.java\",\n" +
			"#      \"lineNumber\" : 135,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.handler.ScopedHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"ContextHandlerCollection.java\",\n" +
			"#      \"lineNumber\" : 255,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.handler.ContextHandlerCollection\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"HandlerCollection.java\",\n" +
			"#      \"lineNumber\" : 154,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.handler.HandlerCollection\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"HandlerWrapper.java\",\n" +
			"#      \"lineNumber\" : 116,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.handler.HandlerWrapper\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"Server.java\",\n" +
			"#      \"lineNumber\" : 370,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.Server\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handleRequest\",\n" +
			"#      \"fileName\" : \"AbstractHttpConnection.java\",\n" +
			"#      \"lineNumber\" : 494,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.AbstractHttpConnection\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"headerComplete\",\n" +
			"#      \"fileName\" : \"AbstractHttpConnection.java\",\n" +
			"#      \"lineNumber\" : 971,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.AbstractHttpConnection\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"headerComplete\",\n" +
			"#      \"fileName\" : \"AbstractHttpConnection.java\",\n" +
			"#      \"lineNumber\" : 1033,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"parseNext\",\n" +
			"#      \"fileName\" : \"HttpParser.java\",\n" +
			"#      \"lineNumber\" : 644,\n" +
			"#      \"className\" : \"org.eclipse.jetty.http.HttpParser\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"parseAvailable\",\n" +
			"#      \"fileName\" : \"HttpParser.java\",\n" +
			"#      \"lineNumber\" : 235,\n" +
			"#      \"className\" : \"org.eclipse.jetty.http.HttpParser\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"AsyncHttpConnection.java\",\n" +
			"#      \"lineNumber\" : 82,\n" +
			"#      \"className\" : \"org.eclipse.jetty.server.AsyncHttpConnection\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"SslConnection.java\",\n" +
			"#      \"lineNumber\" : 196,\n" +
			"#      \"className\" : \"org.eclipse.jetty.io.nio.SslConnection\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"handle\",\n" +
			"#      \"fileName\" : \"SelectChannelEndPoint.java\",\n" +
			"#      \"lineNumber\" : 696,\n" +
			"#      \"className\" : \"org.eclipse.jetty.io.nio.SelectChannelEndPoint\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"run\",\n" +
			"#      \"fileName\" : \"SelectChannelEndPoint.java\",\n" +
			"#      \"lineNumber\" : 53,\n" +
			"#      \"className\" : \"org.eclipse.jetty.io.nio.SelectChannelEndPoint$1\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"runJob\",\n" +
			"#      \"fileName\" : \"QueuedThreadPool.java\",\n" +
			"#      \"lineNumber\" : 608,\n" +
			"#      \"className\" : \"org.eclipse.jetty.util.thread.QueuedThreadPool\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"run\",\n" +
			"#      \"fileName\" : \"QueuedThreadPool.java\",\n" +
			"#      \"lineNumber\" : 543,\n" +
			"#      \"className\" : \"org.eclipse.jetty.util.thread.QueuedThreadPool$3\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    }, {\n" +
			"#      \"methodName\" : \"run\",\n" +
			"#      \"fileName\" : \"Thread.java\",\n" +
			"#      \"lineNumber\" : 745,\n" +
			"#      \"className\" : \"java.lang.Thread\",\n" +
			"#      \"nativeMethod\" : false\n" +
			"#    } ],\n" +
			"#    \"message\" : \"Metric 'docker.network.eth0.rxerrors' not found\"\n" +
			"#  } ]\n" +
			"#}\n"
			;
}
