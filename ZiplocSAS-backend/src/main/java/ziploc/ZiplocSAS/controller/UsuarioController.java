package ziploc.ZiplocSAS.controller;

import ziploc.ZiplocSAS.dto.request.CrearUsuarioRequest;
import ziploc.ZiplocSAS.dto.response.ApiResponse;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Registrar usuario")
    public ResponseEntity<ApiResponse<Usuario>> registrar(@Valid @RequestBody CrearUsuarioRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.ok("Registrado", usuarioService.registrar(req.getNombre(), req.getEmail(), req.getCedula()))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID")
    public ResponseEntity<ApiResponse<Usuario>> buscarPorId(@PathVariable String id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("Encontrado", u)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("No encontrado")));
    }

    @GetMapping("/cedula/{cedula}")
    @Operation(summary = "Buscar por cédula")
    public ResponseEntity<ApiResponse<Usuario>> buscarPorCedula(@PathVariable String cedula) {
        return usuarioService.buscarPorCedula(cedula)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("Encontrado", u)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("No encontrado")));
    }

    @GetMapping
    @Operation(summary = "Listar todos")
    public ResponseEntity<ApiResponse<List<Usuario>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Usuarios", usuarioService.listarTodos()));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking por puntos (BST inorden)")
    public ResponseEntity<ApiResponse<List<Usuario>>> ranking() {
        return ResponseEntity.ok(ApiResponse.ok("Ranking", usuarioService.listarOrdenadosPorPuntos()));
    }

    @GetMapping("/nivel/{nivel}")
    @Operation(summary = "Filtrar por nivel")
    public ResponseEntity<ApiResponse<List<Usuario>>> porNivel(@PathVariable NivelUsuario nivel) {
        return ResponseEntity.ok(ApiResponse.ok("Nivel " + nivel, usuarioService.buscarPorNivel(nivel)));
    }

    @GetMapping("/rango-puntos")
    @Operation(summary = "Usuarios en rango de puntos")
    public ResponseEntity<ApiResponse<List<Usuario>>> porRangoPuntos(
            @RequestParam int min, @RequestParam int max) {
        return ResponseEntity.ok(ApiResponse.ok("Rango", usuarioService.buscarPorRangoPuntos(min, max)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponse<Usuario>> actualizar(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String email) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Actualizado", usuarioService.actualizar(id, nombre, email)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String id) {
        try { usuarioService.eliminar(id); return ResponseEntity.ok(ApiResponse.ok("Eliminado", null)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }
}