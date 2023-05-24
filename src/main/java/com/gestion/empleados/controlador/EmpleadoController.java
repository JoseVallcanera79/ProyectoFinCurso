package com.gestion.empleados.controlador;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.gestion.empleados.servicio.EmailAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestion.empleados.entidades.Empleado;
import com.gestion.empleados.servicio.EmpleadoService;
import com.gestion.empleados.util.paginacion.PageRender;
import com.gestion.empleados.util.reportes.EmpleadoExporterExcel;
import com.gestion.empleados.util.reportes.EmpleadoExporterPDF;
import com.lowagie.text.DocumentException;

@Controller
public class EmpleadoController {

	@Autowired
	private EmpleadoService empleadoService;

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public String handleEmailAlreadyExistsException(EmailAlreadyExistsException e, RedirectAttributes flash) {
		flash.addFlashAttribute("error", e.getMessage());
		return "redirect:/form";
	}

	@GetMapping("/ver/{id}")
	public String verDetallesDelEmpleado(@PathVariable(value = "id") Long id,Map<String,Object> modelo,RedirectAttributes flash) {
		Empleado empleado = empleadoService.findOne(id);
		if(empleado == null) {
			flash.addFlashAttribute("error", "El empleado no existe en la base de datos");
			return "redirect:/listar";
		}
		
		modelo.put("empleado",empleado);
		modelo.put("titulo", "Detalles del empleado " + empleado.getNombre());
		return "ver";
	}
	
	@GetMapping({"/","/listar",""})
	public String listarEmpleados(@RequestParam(name = "page",defaultValue = "0") int page,Model modelo) {
		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Empleado> empleados = empleadoService.findAll(pageRequest);
		PageRender<Empleado> pageRender = new PageRender<>("/listar", empleados);
		
		modelo.addAttribute("titulo","Listado de empleados");
		modelo.addAttribute("empleados",empleados);
		modelo.addAttribute("page", pageRender);
		
		return "listar";
	}
	
	@GetMapping("/form")
	public String mostrarFormularioDeRegistrarCliente(Map<String,Object> modelo) {
		Empleado empleado = new Empleado();
		modelo.put("empleado", empleado);
		modelo.put("titulo", "Registro de empleados");
		return "form";
	}
	
	/*@PostMapping("/form")
	public String guardarEmpleado(@Valid Empleado empleado,BindingResult result,Model modelo,RedirectAttributes flash,SessionStatus status) throws EmailAlreadyExistsException {
		if(result.hasErrors()) {
			modelo.addAttribute("titulo", "Registro de cliente");
			return "form";
		}
		
		String mensaje = (empleado.getId() != null) ? "El empleado ha sido editato con exito" : "Empleado registrado con exito";
		
		empleadoService.save(empleado);
		status.setComplete();
		flash.addFlashAttribute("success", mensaje);
		return "redirect:/listar";
	}*/
	@PostMapping("/form")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result, Model modelo, RedirectAttributes flash, SessionStatus status) {
		if (result.hasErrors()) {
			modelo.addAttribute("titulo", "Registro de cliente");
			return "form";
		}

		try {
			String mensaje = (empleado.getId() != null) ? "El empleado ha sido editado con éxito" : "Empleado registrado con éxito";
			empleadoService.save(empleado);
			status.setComplete();
			flash.addFlashAttribute("success", mensaje);
			return "redirect:/listar";
		} catch (EmailAlreadyExistsException e) {
			result.rejectValue("email", "error.email", e.getMessage());
			modelo.addAttribute("titulo", "Registro de cliente");
			return "form";
		}
	}


	@GetMapping("/form/{id}")
	public String editarEmpleado(@PathVariable(value = "id") Long id,Map<String, Object> modelo,RedirectAttributes flash) {
		Empleado empleado = null;
		if(id > 0) {
			empleado = empleadoService.findOne(id);
			if(empleado == null) {
				flash.addFlashAttribute("error", "El ID del empleado no existe en la base de datos");
				return "redirect:/listar";
			}
		}
		else {
			flash.addFlashAttribute("error", "El ID del empleado no puede ser cero");
			return "redirect:/listar";
		}
		
		modelo.put("empleado",empleado);
		modelo.put("titulo", "Edición de empleado");
		return "form";
	}
	
	@GetMapping("/eliminar/{id}")
	public String eliminarCliente(@PathVariable(value = "id") Long id,RedirectAttributes flash) {
		if(id > 0) {
			empleadoService.delete(id);
			flash.addFlashAttribute("success", "Empleado eliminado con exito");
		}
		return "redirect:/listar";
	}
	
	@GetMapping("/exportarPDF")
	public void exportarListadoDeEmpleadosEnPDF(HttpServletResponse response) throws DocumentException, IOException {
		response.setContentType("application/pdf");
		
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String fechaActual = dateFormatter.format(new Date());
		
		String cabecera = "Content-Disposition";
		String valor = "attachment; filename=Empleados_" + fechaActual + ".pdf";
		
		response.setHeader(cabecera, valor);
		
		List<Empleado> empleados = empleadoService.findAll();
		
		EmpleadoExporterPDF exporter = new EmpleadoExporterPDF(empleados);
		exporter.exportar(response);
	}
	
	@GetMapping("/exportarExcel")
	public void exportarListadoDeEmpleadosEnExcel(HttpServletResponse response) throws DocumentException, IOException {
		response.setContentType("application/octet-stream");
		
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String fechaActual = dateFormatter.format(new Date());
		
		String cabecera = "Content-Disposition";
		String valor = "attachment; filename=Empleados_" + fechaActual + ".xlsx";
		
		response.setHeader(cabecera, valor);
		
		List<Empleado> empleados = empleadoService.findAll();
		
		EmpleadoExporterExcel exporter = new EmpleadoExporterExcel(empleados);
		exporter.exportar(response);
	}
}
