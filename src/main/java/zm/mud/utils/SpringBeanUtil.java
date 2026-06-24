package zm.mud.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanUtil {
    private static final Logger logger = LogManager.getLogger(SpringBeanUtil.class);

    private static ApplicationContext ctx;


    public static <T> T getBean(Class<T> t){
        try{
            T bean = ctx.getBean(t);
            return (T)bean;
        }catch(Exception e){
            logger.debug(e);
        }
       
        return null;
    }

    public static <T> T getBean(@NonNull String beanId,Class<T> t){
        try{
            T bean = (T) ctx.getBean(beanId);
            return (T)bean;
        }catch(Exception e){
            logger.debug(e);
        }
       
        return null;
    } 

     
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeanUtil.ctx = applicationContext;
    }

}
