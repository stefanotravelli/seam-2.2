package org.jboss.seam.test.integration;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class MessagingTest
    extends SeamTest
{
    @Test
    public void delayForStartup() 
        throws InterruptedException 
    {
        // need to delay a bit to make sure the messaging system is running
        // really only needed when running this test in isolation
        Thread.sleep(3000);
    }
    
    @Test(dependsOnMethods={"delayForStartup"})
    public void publishToTopic()
        throws Exception
    {
        final SimpleReference<String> messageText = new SimpleReference<String>();
        
        new FacesRequest() {
            @Override
            protected void invokeApplication()
                throws Exception 
            {
                Contexts.getApplicationContext().set("testMessage", messageText);
                invokeAction("#{testTopic.publish}");
            }
        }.run();      

        // need to delay a bit to make sure the message is delivered
        // might need 
        Thread.sleep(2000);
        
        assert messageText.getValue().equals("message for topic");
    }
    
    @Test(dependsOnMethods={"delayForStartup"})
    public void sendToQueue()
        throws Exception
    {
        final SimpleReference<String> messageText = new SimpleReference<String>();
        
        new FacesRequest() {
            @Override
            protected void invokeApplication()
                throws Exception 
            {
                Contexts.getApplicationContext().set("testMessage", messageText);
                invokeAction("#{testQueue.send}");
            }
        }.run();      

        // need to delay a bit to make sure the message is delivered
        // might need 
        Thread.sleep(2000);
        
        assert messageText.getValue().equals("message for queue");
    }


    @Name("testTopic")
    public static class TopicBean {
        @In 
        private TopicPublisher testPublisher; 
        
        @In 
        private TopicSession topicSession; 
        
        public void publish() 
            throws JMSException 
        { 
            testPublisher.publish(topicSession.createTextMessage("message for topic")); 
        } 
    }
    
    @Name("testQueue")
    public static class QueueBean {
        @In 
        private QueueSender testSender;
        
        @In 
        private QueueSession queueSession;
        
        public void send() throws JMSException { 
            testSender.send(queueSession.createTextMessage("message for queue")); 
        } 
    }
    
    @MessageDriven(activationConfig={
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Topic"),
        @ActivationConfigProperty(propertyName="destination",     propertyValue="topic/testTopic")
    })
    @Name("testTopicListener")
    static public class TestTopicListener 
        implements MessageListener
    {
        @In
        private SimpleReference<String> testMessage;

        public void onMessage(Message msg)
        {
            try {
                testMessage.setValue(((TextMessage) msg).getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @MessageDriven(activationConfig={
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination",     propertyValue="queue/testQueue")
    })
    @Name("testQueueListener")
    static public class TestQueueListener 
        implements MessageListener
    {
        @In
        private SimpleReference<String> testMessage;

        public void onMessage(Message msg)
        {
            try {
                testMessage.setValue(((TextMessage) msg).getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    static class SimpleReference<T> {
        T value;
        public SimpleReference() {            
        }
        public SimpleReference(T value) {
            setValue(value);
        }
        public T getValue() { 
            return value; 
        }
        public void setValue(T value) {
            this.value = value;
        }
    }
}
