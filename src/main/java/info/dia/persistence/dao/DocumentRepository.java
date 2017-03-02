package info.dia.persistence.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import info.dia.persistence.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
