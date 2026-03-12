import Classes.Session;
import com.google.gson.JsonObject;

void main() {
    try{
        Session session = new Session("10.10.20.251", "api", "ParadiseGreen1!");
        LinkedList<String> subscriptions = new LinkedList<>();
        for (int i = 0; i < 292; i ++){
            subscriptions.add("/api/channel/" + i);
        }
        session.add_subscriptions(subscriptions);
        while (true){
            JsonObject obj = session.get_subscribed_content();
            if (obj != null){
                System.out.println(obj);
            } else{
                System.out.println("uh oh");
            }
        }
    } catch (Exception e) {
        System.out.println(e.getMessage());
        for (StackTraceElement i: e.getStackTrace()){
            System.out.println(i);
        }
    }
}