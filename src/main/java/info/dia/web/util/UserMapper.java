package info.dia.web.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import info.dia.persistence.model.User;
import info.dia.response.UserResoponseDto;

public class UserMapper {

	public static info.dia.response.UserResoponseDto map(User user) {
			UserResoponseDto dto = new UserResoponseDto();
			dto.setId(user.getId());
			dto.setEmail(user.getEmail());
			dto.setFirstName(user.getFirstName());
			dto.setLastName(user.getLastName());
			dto.setRoles(user.getRoles());
			return dto;
	}
	public static List<UserResoponseDto> map(Page<User> users) {
		List<UserResoponseDto> dtos = new ArrayList<UserResoponseDto>();
		for (User user: users) {
			dtos.add(map(user));
		}
		return dtos;
	}
}
