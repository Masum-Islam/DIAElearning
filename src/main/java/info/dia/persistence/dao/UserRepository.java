package info.dia.persistence.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import info.dia.persistence.model.User;

public interface UserRepository extends JpaRepository<User, Long>,QueryDslPredicateExecutor<User> {
	
    User findByEmail(String email);
    void delete(User user);   
    List<User> findByRolesName(String name);
    
    List<User> findByRolesNameAndEmailIgnoreCaseContainingOrFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContaining(String roleName,String email,String firstName,String lastName);
    

}
