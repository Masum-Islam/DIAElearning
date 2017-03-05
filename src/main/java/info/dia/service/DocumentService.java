package info.dia.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import info.dia.persistence.dao.AssignmentRepository;
import info.dia.persistence.dao.DocumentRepository;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.Document;
import info.dia.web.dto.DocumentDto;

@Service
@Transactional
public class DocumentService implements IDocumentService {
	
	@Autowired
	private DocumentRepository documentRepository;
	
	@Autowired
	private AssignmentRepository assignmentRepository;

	@Override
	public Document saveOrUpdate(DocumentDto documentDto) {
		
		Document document = new Document();
		
		document.setName(documentDto.getName());
		document.setLocation(documentDto.getLocation());
		document.setSize(documentDto.getSize());
		document.setStatus(documentDto.getStatus());
		document.setType(documentDto.getType());
		document.setUserId(documentDto.getUserId());
		document.setCreateOn(new Date());
		
		Assignment assignment = assignmentRepository.findOne(documentDto.getAssignmentId());
		
		document.setAssignment(assignment);
		
		return documentRepository.save(document);
	}

	@Override
	public List<Document> getAllDocumentsByAssignmenmt(Assignment assignment) {
		return documentRepository.findAllByAssignment(assignment);
	}

	@Override
	public Document findById(long id) {
		return documentRepository.getOne(id);
	}

	@Override
	public Document getDocumentByIdAndAssignmentAndUser(Long id, Assignment assignment, Long userId) {
		return documentRepository.findByIdAndAssignmentAndUserId(id, assignment, userId);
	}

	@Override
	public Document getDocumentByIdAndAssignmentIdAndUser(Long id, Long assignmentId, Long userId) {
		return documentRepository.findByIdAndAssignmentIdAndUserId(id, assignmentId, userId);
	}

}
