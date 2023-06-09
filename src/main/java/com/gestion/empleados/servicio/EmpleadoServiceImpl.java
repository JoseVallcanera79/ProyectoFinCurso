package com.gestion.empleados.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.empleados.entidades.Empleado;
import com.gestion.empleados.repositorios.EmpleadoRepository;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

	@Autowired
	private EmpleadoRepository empleadoRepository;


		/*public void registerEmpleado(Empleado empleado) {
			String email = empleado.getEmail();
			if (empleadoRepository.findByEmail(email) != null) {
				// El correo electrónico ya está registrado
				// Puedes lanzar una excepción, mostrar un mensaje de error, etc.
			} else {
				// Guardar el usuario en la base de datos
				empleadoRepository.save(empleado);
			}
		}*/
	@Override
	@Transactional
	public void save(Empleado empleado) throws EmailAlreadyExistsException {
		String email = empleado.getEmail();
		if (empleadoRepository.findByEmail(email) != null) {
			// El correo electrónico ya está registrado
			// Puedes lanzar una excepción, mostrar un mensaje de error, etc.
			throw new EmailAlreadyExistsException("El email ya existe en la base de datos");
		} else {
			// Guardar el usuario en la base de datos
			empleadoRepository.save(empleado);
		}
	}


	@Override
	@Transactional(readOnly = true)
	public List<Empleado> findAll() {
		return (List<Empleado>) empleadoRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Empleado> findAll(Pageable pageable) {
		return empleadoRepository.findAll(pageable);
	}



	@Override
	@Transactional
	public void delete(Long id) {
		empleadoRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Empleado findOne(Long id) {
		return empleadoRepository.findById(id).orElse(null);
	}
	
}
