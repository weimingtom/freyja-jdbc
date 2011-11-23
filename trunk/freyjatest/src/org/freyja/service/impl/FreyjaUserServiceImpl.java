package org.freyja.service.impl;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.annotation.Resources;

import org.freyja.bean.Property;
import org.freyja.bean.User;
import org.freyja.bean.UserProperty;
import org.freyja.dao.BaseDAO;
import org.freyja.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("freyjaService")
public class FreyjaUserServiceImpl implements IUserService {
	@Resource(name = "freyja")
	private BaseDAO baseDAO;

	public void testFind() {
		for (int i = 0; i < 10; i++) {
			List<User> list = baseDAO.find(User.class, "level < ?", i);
		}
	}

	public void testSave() {
		for (int i = 0; i < 5000; i++) {
			User user = new User();
			user.setAge(i);
			user.setLevel(i);
			user.setName(System.currentTimeMillis() + "");
			user.setNickName("nickName:" + user.getName() + "");
			baseDAO.save(user);
		}
	}

	public void testUpdate() {
		List<User> list = baseDAO.find(User.class);
		for (User user : list) {
			user.setName(System.currentTimeMillis() + "");
			baseDAO.update(user);

		}
	}

	public void testSave2() {
		List<User> list = baseDAO.find(User.class);
		Random r = new Random();
		List<Property> propertys = baseDAO.find(Property.class);
		for (User user : list) {
			for (Property property : propertys) {
				UserProperty up = new UserProperty();
				up.setUser(user);
				up.setPropertyId(property.getId());
				up.setUid(user.getId());
				up.setNumber(r.nextInt(100));
				baseDAO.save(up);
			}
		}

	}

	@Override
	public void findUserProperty() {

		for (int i = 0; i < 1000; i++) {
			List<UserProperty> list = baseDAO.find(UserProperty.class,
					"user.level < ?", i);
		}
		// for (int i = 0; i < 1000; i++) {
		// List<UserProperty> list = baseDAO.find(UserProperty.class,
		// "user.level < ? and property.type = 1", i);
		//
		// }

	}
}
