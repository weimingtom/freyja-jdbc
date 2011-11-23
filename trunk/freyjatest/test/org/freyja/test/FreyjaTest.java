package org.freyja.test;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.persistence.Access;

import org.freyja.bean.Property;
import org.freyja.bean.User;
import org.freyja.bean.UserProperty;
import org.freyja.dao.BaseDAO;
import org.freyja.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-db-freyja.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
public class FreyjaTest {
	@Resource(name="freyja")
	private BaseDAO baseDAO;
	@Resource(name="freyjaService")	
	private IUserService userService;

//	 @Test
	public void testSave() {	 
		
		long l1 = System.currentTimeMillis();
		userService.testSave();
//		for (int i = 0; i < 5000; i++) {
//			User user = new User();
//			user.setAge(i);
//			user.setLevel(i);
//			user.setName(System.currentTimeMillis() + "");
//			user.setNickName("nickName:" + user.getName() + "");
//			baseDAO.save(user);
//		}
		long l2 = System.currentTimeMillis();
		System.out.println("freyja save() 5000个对象耗时：" + (l2 - l1) / 1000);
	}

//	 @Test
	public void testUpdate() {
		long l1 = System.currentTimeMillis();
		
		userService.testUpdate();
//		List<User> list = baseDAO.find(User.class);
//		for (User user : list) {
//			user.setName(System.currentTimeMillis() + "");
//			baseDAO.update(user);
//
//		}
		long l2 = System.currentTimeMillis();
		System.out.println("freyja update() 5000个对象耗时：" + (l2 - l1) / 1000);
	}

	@Test
	public void testFind() {
	
		
		long l1 =System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			userService.testFind();
			}
		long l2 = System.nanoTime();
		System.out.println("freyja find() 5000个对象耗时：" + (l2 - l1) / 1000/1000);
	}
//	@Test
	public void aa(){
		
		
	}

//	 @Test
	public void testSave2() {
		long l1 = System.currentTimeMillis();
		
		userService.testSave2();
		

		long l2 = System.currentTimeMillis();
		System.out.println("freyja save() 5000个对象耗时：" + (l2 - l1) / 1000);
	}
//	@Test
	public void findUserProperty() {
	
		
		long l1 = System.currentTimeMillis();
		userService.findUserProperty();

		long l2 = System.currentTimeMillis();
		System.out.println("freyja findUserProperty() 5000个对象耗时：" + (l2 - l1) / 1000);
	}
}
