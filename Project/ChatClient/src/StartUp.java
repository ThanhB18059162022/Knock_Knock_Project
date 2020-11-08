import java.util.List;

import model.communication.*;
import model.sendmodel.*;
import socket.Client;

public class StartUp {

	public static void main(String[] args) {

		Thread A = new Thread(()->{
			send();
		});
		
		Thread B = new Thread(()->{
			receive();
		});
				
		A.start();
		B.start();
		
		try {
			A.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void send()
	{

		Client client = new Client();
//		LoginModel model = new LoginModel("admin", "1234");
		LoginModel model = new LoginModel("user2", "1234");

		CPackage CPackage = new CPackage(Type.AUTHENTICATION, new Request(Name.LOGIN, model));
		client.send(CPackage);
		boolean success = (boolean)client.receive().getRequest().getContent();

		if (success) {

			System.out.println("Success");
			CPackage = new CPackage(Type.ACCOUNT, new Request(Name.GET, Name.GET));
			client.send(CPackage);
			Person person = (Person)client.receive().getRequest().getContent();
			System.out.println(person.getName());
//			
//			System.out.println("\n\n");
			
//			CPackage = new CPackage(Type.FRIEND, new Request(Name.GET, Name.GET));
//			client.send(CPackage);
//			@SuppressWarnings("unchecked")
//			List<Person> friends = (List<Person>)client.receive().getRequest().getContent();
//			for (Person person2 : friends) {
//				System.out.println(person2.getName());
//			}
			
			CPackage = new CPackage(Type.ROOM, new Request(Name.GET, Name.GET));
			client.send(CPackage);
			System.out.println("\n Room \n");
			@SuppressWarnings("unchecked")
			List<Room> rooms = (List<Room>)client.receive().getRequest().getContent();
			
			for (Room room : rooms) {
				System.out.println(room.getName());
				for (Person mem : room.getMembers()) {
					System.out.println(mem.getName());
				}
				
				System.out.println("\n");
			}
			
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			FileInfo file = new FileInfo();
			file.setName("Hallo");
			Message model1 = new Message(person, rooms.get(0), file, false, null);
			
			CPackage = new CPackage(Type.MESSAGE, new Request(Name.ADD, model1));
			client.send(CPackage);
			
		}
	}
	
	public static void receive()
	{
		Client client = new Client();
		LoginModel model = new LoginModel("admin", "1234");

		CPackage CPackage = new CPackage(Type.AUTHENTICATION, new Request(Name.LOGIN, model));
		client.send(CPackage);
		boolean success = (boolean)client.receive().getRequest().getContent();

		if (success) {

			System.out.println("Success");
			CPackage = new CPackage(Type.ACCOUNT, new Request(Name.GET, Name.GET));
			client.send(CPackage);
			Person person = (Person)client.receive().getRequest().getContent();
			System.out.println(person.getName());
			
			System.out.println("\n\n");			
			
			Message msg = (Message)client.receive().getRequest().getContent();
			
			System.out.println(msg.getContent().getName());
		}
	}
}
