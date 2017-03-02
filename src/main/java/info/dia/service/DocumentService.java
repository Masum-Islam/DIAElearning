package info.dia.service;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import info.dia.persistence.dao.DocumentRepository;
import info.dia.persistence.model.Document;
import info.dia.web.dto.DocumentDto;

@Service
@Transactional
public class DocumentService implements IDocumentService {
	
	@Autowired
	private DocumentRepository documentRepository;

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
		document.setAssignmentId(documentDto.getAssignmentId());
		
		return documentRepository.save(document);
	}

}
