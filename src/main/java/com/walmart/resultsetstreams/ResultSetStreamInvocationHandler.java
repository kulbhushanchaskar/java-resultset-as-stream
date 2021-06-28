package com.walmart.resultsetstreams;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ResultSetStreamInvocationHandler<T> implements InvocationHandler{

    private Stream<T> stream;
    private PreparedStatement st;
    private ResultSet rs;
    
    public void setup(PreparedStatement st, Function<ResultSet, T> mappingFunction) throws SQLException{
        this.st = st;
        rs = st.executeQuery();
        stream = Stream.generate(new ResultSetSupplier(rs, mappingFunction));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        
        if (method == null)
            throw new RuntimeException("null method null");
        
        if (method.getName().equals("close") && args == null){
            if (st != null){
                st.close();
            }
        }
        return method.invoke(stream, args);//invokes the actual method call
    }
    
    private class ResultSetSupplier implements Supplier<T>{        
        private final ResultSet rs;
        private final Function<ResultSet, T> mappingFunction;

        private ResultSetSupplier(ResultSet rs, Function<ResultSet, T> mappingFunction) {
            this.rs = rs;
            this.mappingFunction = mappingFunction;
        }

        @Override
        public T get() {
            try {
                if (rs.next())
                    return mappingFunction.apply(rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    }

