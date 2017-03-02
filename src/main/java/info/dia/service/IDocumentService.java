package info.dia.service;

import info.dia.persistence.model.Document;
import info.dia.web.dto.DocumentDto;

public interface IDocumentService {
	
	Document saveOrUpdate(DocumentDto documentDto);

}
