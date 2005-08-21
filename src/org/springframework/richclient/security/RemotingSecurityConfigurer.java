package org.springframework.richclient.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.acegisecurity.Authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.remoting.caucho.BurlapProxyFactoryBean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean;
import org.springframework.richclient.application.Application;

/**
 * Correctly configures the username and password on Spring's remoting proxy
 * factory beans.
 * 
 * <P>
 * This bean works with "Spring Remoting Proxy Factories" defined in the
 * application context. Presently this includes the following Spring classes:
 * {@link HessianProxyFactoryBean},{@link BurlapProxyFactoryBean}and {@link
 * JaxRpcPortProxyFactoryBean}.
 * </p>
 * 
 * <P>
 * This bean listens for any <code>ClientSecurityEvent</code> and responds as
 * follows:
 * </p>
 * 
 * <P>
 * Upon receipt of a {@link LoginEvent}, any Spring Remoting Proxy Factories
 * will be located. Each located bean will have its username and password
 * methods set to the <code>LoginEvent</code>'s principal and credentials
 * respectively.
 * </p>
 * 
 * <P>
 * Upon receipt of a {@link LogoutEvent}, any Spring Remoting Proxy Factories
 * will be located. Each located bean will have its username and password
 * methods set to <code>null</code>.
 * </p>
 * 
 * @author Ben Alex
 */
public class RemotingSecurityConfigurer implements ApplicationListener {
    //~ Static fields/initializers
    // =============================================

    protected static final Log logger = LogFactory.getLog(RemotingSecurityConfigurer.class);

    //~ Methods
    // ================================================================

    public void onApplicationEvent(ApplicationEvent event) {
        if (logger.isDebugEnabled() && event instanceof ClientSecurityEvent) {
            logger.debug("Processing event: " + event.toString());
        }

        if (event instanceof LoginEvent) {
            Authentication authentication = (Authentication)event.getSource();
            updateExporters(authentication.getPrincipal().toString(), authentication.getCredentials().toString());
        }
        else if (event instanceof LogoutEvent) {
            updateExporters(null, null);
        }
    }

    private Object[] getExporters() {
    	ListableBeanFactory bf = (ListableBeanFactory) Application.services().getBeanFactory();
        ApplicationContext appCtx = Application.services().getApplicationContext();
        List list = new Vector();

        {
            Map map = bf.getBeansOfType(HessianProxyFactoryBean.class, false, true);
            Iterator iter = map.keySet().iterator();

            while (iter.hasNext()) {
                String beanName = (String)iter.next();
                Object factoryBean = bf.getBean("&" + beanName);
                list.add(factoryBean);
            }
        }

        {
            Map map = appCtx.getBeansOfType(BurlapProxyFactoryBean.class, false, true);
            Iterator iter = map.keySet().iterator();

            while (iter.hasNext()) {
                String beanName = (String)iter.next();
                Object factoryBean = appCtx.getBean("&" + beanName);
                list.add(factoryBean);
            }
        }

        {
            Map map = appCtx.getBeansOfType(JaxRpcPortProxyFactoryBean.class, false, true);
            Iterator iter = map.keySet().iterator();

            while (iter.hasNext()) {
                String beanName = (String)iter.next();
                Object factoryBean = appCtx.getBean("&" + beanName);
                list.add(factoryBean);
            }
        }

        return list.toArray();
    }

    private void updateExporters(String username, String password) {
        Object[] factories = getExporters();

        for (int i = 0; i < factories.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating " + factories[i].toString() + " to username: " + username
                        + "; password: [PROTECTED]");
            }

            try {
                Method method = factories[i].getClass().getMethod("setUsername", new Class[] { String.class });
                method.invoke(factories[i], new Object[] { username });
            }
            catch (NoSuchMethodException ignored) {
                logger.error("Could not call setter", ignored);
            }
            catch (IllegalAccessException ignored) {
                logger.error("Could not call setter", ignored);
            }
            catch (InvocationTargetException ignored) {
                logger.error("Could not call setter", ignored);
            }

            try {
                Method method = factories[i].getClass().getMethod("setPassword", new Class[] { String.class });
                method.invoke(factories[i], new Object[] { password });
            }
            catch (NoSuchMethodException ignored) {
                logger.error("Could not call setter", ignored);
            }
            catch (IllegalAccessException ignored) {
                logger.error("Could not call setter", ignored);
            }
            catch (InvocationTargetException ignored) {
                logger.error("Could not call setter", ignored);
            }
        }
    }
}