package ziploc.ZiplocSAS.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ziploc.ZiplocSAS.dto.request.*;
import ziploc.ZiplocSAS.dto.response.*;
import ziploc.ZiplocSAS.model.*;
import ziploc.ZiplocSAS.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrar(
            @Valid @RequestBody RegistrarUsuarioRequest req) {
        Usuario u = usuarioService.registrar(req.getNombre(), req.getEmail(), req.getCedula());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario registrado", UsuarioResponse.from(u)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarTodos() {
        List<UsuarioResponse> lista = usuarioService.listarTodos().stream()
                .map(UsuarioResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("OK", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscarPorId(@PathVariable String id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("OK", UsuarioResponse.from(u))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Usuario no encontrado: " + id)));
    }

    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscarPorCedula(@PathVariable String cedula) {
        return usuarioService.buscarPorCedula(cedula)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("OK", UsuarioResponse.from(u))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No encontrado con cédula: " + cedula)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ActualizarUsuarioRequest req) {
        Usuario u = usuarioService.actualizar(id, req.getNombre(), req.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", UsuarioResponse.from(u)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado", null));
    }

    @GetMapping("/puntos/ranking")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> ranking() {
        List<UsuarioResponse> lista = usuarioService.listarOrdenadosPorPuntos()
                .stream().map(UsuarioResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Ranking por puntos", lista));
    }

    @GetMapping("/puntos/rango")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> porRangoPuntos(
            @RequestParam int min, @RequestParam int max) {
        List<UsuarioResponse> lista = usuarioService.buscarPorRangoPuntos(min, max)
                .stream().map(UsuarioResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios en rango", lista));
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> porNivel(
            @PathVariable NivelUsuario nivel) {
        List<UsuarioResponse> lista = usuarioService.buscarPorNivel(nivel)
                .stream().map(UsuarioResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios nivel " + nivel, lista));
    }
}