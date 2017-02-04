package info.dia.persistence.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import info.dia.persistence.model.Group;
import info.dia.persistence.model.User;

public interface GroupRepository extends JpaRepository<Group, Long>{
	
	List<Group> findByUser(User user);
	
	Group findByUserAndNameIgnoreCase(User user,String name);
	
}
