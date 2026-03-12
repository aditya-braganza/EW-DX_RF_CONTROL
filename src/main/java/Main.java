import Classes.Session;
import com.google.gson.JsonObject;

void main() {
    try{
        Session session = new Session("10.10.20.251", "api", "ParadiseGreen1!");
        session.subscribe("/api/channel/0");
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