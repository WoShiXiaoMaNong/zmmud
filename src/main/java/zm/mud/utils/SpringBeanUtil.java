package zm.mud.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
            logger.error(e);
        }
       
        return null;
    }

     
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeanUtil.ctx = applicationContext;
    }

}
