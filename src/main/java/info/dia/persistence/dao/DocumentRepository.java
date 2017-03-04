package info.dia.persistence.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

	List<Document> findAllByAssignment(Assignment assignment);
}
