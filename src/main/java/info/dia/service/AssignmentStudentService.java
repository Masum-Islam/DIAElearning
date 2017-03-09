package info.dia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mysema.query.BooleanBuilder;

import info.dia.persistence.dao.AssignmentStudentRepository;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.persistence.model.QAssignmentStudent;
import info.dia.web.dto.SearchDTO;

@Service
@Transactional
public class AssignmentStudentService implements IAssignmentStudentService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AssignmentStudentRepository assignmentStudentRepository;
	

	@Override
	public Page<AssignmentStudent> findAllByAssignment(Assignment assignment, Pageable pageable) {
		return assignmentStudentRepository.findAllByAssignment(assignment, pageable);
	}


	@Override
	public Page<AssignmentStudent> searchAssignmentStudent(Assignment assignment, SearchDTO dto, Pageable pageable) {
		
		BooleanBuilder b = new BooleanBuilder();
		
		QAssignmentStudent qAssignmentStudent = QAssignmentStudent.assignmentStudent;
		
		if (dto!=null) {
			
			if (!StringUtils.isEmpty(dto.getSearchString()) && dto.getAssignmentStatus()!=null) {
				b = b.and(qAssignmentStudent.email.containsIgnoreCase(dto.getSearchString())
						.or(qAssignmentStudent.status.eq(dto.getAssignmentStatus()))
						.and(qAssignmentStudent.assignment.id.eq(assignment.getId())));
			}else if (!StringUtils.isEmpty(dto.getSearchString())) {
				b = b.and(qAssignmentStudent.email.containsIgnoreCase(dto.getSearchString())
						.and(qAssignmentStudent.assignment.id.eq(assignment.getId())));
			}else if (dto.getAssignmentStatus()!=null) {
				b = b.and(qAssignmentStudent.status.eq(dto.getAssignmentStatus())
						.and(qAssignmentStudent.assignment.id.eq(assignment.getId())));
			}else {
				/*LOGGER.info("Nothing.............");*/
				b = b.and(qAssignmentStudent.assignment.id.eq(assignment.getId()));
			}
		}
		LOGGER.info("Assignment Student Size............."+assignmentStudentRepository.findAll(b, pageable).getTotalElements());
		return assignmentStudentRepository.findAll(b, pageable);
	}


	@Override
	public AssignmentStudent findByAssignmentStudentId(long assignmentStudentId) {
		return assignmentStudentRepository.findOne(assignmentStudentId);
	}


	@Override
	public Page<AssignmentStudent> getAllStudentAssignmentByEmailAndAssignmentStatus(String email,Boolean status,Pageable pageable) {
		return assignmentStudentRepository.findAllByEmailAndAssignmentStatus(email,status,pageable);
	}


	@Override
	public AssignmentStudent saveOrUpdate(AssignmentStudent assignmentStudent) {
		return assignmentStudentRepository.saveAndFlush(assignmentStudent);
	}


	@Override
	public AssignmentStudent getAssignmentStudentByEmailAndAssignmentId(String email, Long assignmentId) {
		return assignmentStudentRepository.findByEmailAndAssignmentId(email, assignmentId);
	}


	

}
