//package org.salmon.remoting.remoting.protocol;
//
//import java.nio.ByteBuffer;
//import static org.junit.Assert.assertTrue;
//import org.junit.Test;
//import org.salmon.remoting.CommandCustomHeader;
//import org.salmon.remoting.protocol.RemotingCommand;
//
//public class RemotingCommandTest {
//	@Test
//	public void testSerialize() {
//		MyHeader myheader = new MyHeader();
//		myheader.setAge(30);
//		myheader.setName("wuwenyu"); 
//		RemotingCommand cmd = RemotingCommand.createRequestCommand(30, myheader);
//		cmd.setRemark("测试json序列化");
//		cmd.setOpaque(111);
//		
//		cmd.markOnewayRPC();
//		cmd.markResponseType();
//		
//		System.out.println(cmd);
//		//序列化前
//		assertTrue(cmd.getCode()==30);
//		assertTrue("1.0.0".equals(cmd.getVersion()));
//		assertTrue(cmd.getOpaque() == 111);
//		assertTrue("11".equals(Integer.toBinaryString(cmd.getFlag())));
//		assertTrue("测试json序列化".equals(cmd.getRemark()));
//		assertTrue(cmd.getExtFields()==null);
//		
//		
//		ByteBuffer header = cmd.encodeHeader(); 
//		ByteBuffer bf = ByteBuffer.allocate(100 + header.limit()); 
//		bf.put(header);
//		bf.put(new byte[100]);
//		bf.flip();
//		System.out.println(cmd);
//		//序列化后
//		assertTrue(cmd.getCode()==30);
//		assertTrue("1.0.0".equals(cmd.getVersion()));
//		assertTrue(cmd.getOpaque() == 111);
//		assertTrue("11".equals(Integer.toBinaryString(cmd.getFlag())));
//		assertTrue("测试json序列化".equals(cmd.getRemark()));
//		assertTrue(cmd.getExtFields()!=null);
//		bf.getInt();
//		
//		bf = bf.slice();
//		//反序列化后 
//		RemotingCommand df = RemotingCommand.decode(bf); 
//		System.out.println(df);
//		assertTrue(df.getCode()==30);
//		assertTrue("1.0.0".equals(df.getVersion()));
//		assertTrue(df.getOpaque() == 111);
//		assertTrue("11".equals(Integer.toBinaryString(df.getFlag())));
//		assertTrue("测试json序列化".equals(df.getRemark()));
//		assertTrue(df.getExtFields()!=null);
//	}
//	
//	public class MyHeader implements CommandCustomHeader { 
//
//		private String name;
//		
//		private int age; 
//
//		@Override
//		public String toString() {
//			return "MyHeader [name=" + name + ", age=" + age + "]";
//		}
//
//		public String getName() {
//			return name;
//		}
//
//		public void setName(String name) {
//			this.name = name;
//		}
//
//		public int getAge() {
//			return age;
//		}
//
//		public void setAge(int age) {
//			this.age = age;
//		} 
//
//	}
//
//}
