package info.dia.persistence.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import info.dia.persistence.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
    User findByEmail(String email);
    void delete(User user);   
    List<User> findByRolesName(String name);
    

}
