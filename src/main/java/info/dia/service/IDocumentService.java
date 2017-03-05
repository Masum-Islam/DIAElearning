package info.dia.service;

import java.util.List;

import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.Document;
import info.dia.web.dto.DocumentDto;

public interface IDocumentService {
	
	Document saveOrUpdate(DocumentDto documentDto);
	
	List<Document> getAllDocumentsByAssignmenmt(Assignment assignment);
	
	Document findById(long id);
	
	Document getDocumentByIdAndAssignmentAndUser(Long id,Assignment assignment,Long userId);
	
	Document getDocumentByIdAndAssignmentIdAndUser(Long id,Long assignmentId,Long userId);
	
}
