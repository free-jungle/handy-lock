package com.github.free.jungle.handy.lock.core;

import java.lang.reflect.Method;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author wangxiaoming
 * @version 1.0
 */
public class SimpleSpelExpressionEvaluator {

    private SpelExpressionParser parser;

    private ParameterNameDiscoverer parameterNameDiscoverer;

    public SimpleSpelExpressionEvaluator() {
    }

    public SimpleSpelExpressionEvaluator(SpelExpressionParser parser,
                                         ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parser = parser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    /**
     * 解析方法参数
     *
     * @param spel       表达式
     * @param rootObject root对象
     * @param method     方法对象
     * @param args       方法参数
     * @return 解析后的值
     */
    public String parseMethodValue(String spel,
                                   Object rootObject,
                                   Method method,
                                   Object[] args) {
        //SPEL上下文
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args,
                parameterNameDiscoverer);
        return parser.parseExpression(spel).getValue(context, String.class);
    }

    public SpelExpressionParser getParser() {
        return parser;
    }

    public void setParser(SpelExpressionParser parser) {
        this.parser = parser;
    }

    public ParameterNameDiscoverer getParameterNameDiscoverer() {
        return parameterNameDiscoverer;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

}
