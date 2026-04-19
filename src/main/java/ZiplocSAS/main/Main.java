package ZiplocSAS.main;

import ZiplocSAS.analytics.AnaliticaMovimientos;
import ZiplocSAS.model.*;
import ZiplocSAS.service.*;
import ZiplocSAS.structures.GrafoTransferencias;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static GestorUsuarios gu = new GestorUsuarios();
    static GestorBilleteras gb = new GestorBilleteras();
    static GestorTransacciones gt = new GestorTransacciones(gu, gb);
    static GestorOperacionesProgramadas gop = new GestorOperacionesProgramadas(gt);
    static GestorRecompensas gr = new GestorRecompensas(gu);
    static AnaliticaMovimientos an = new AnaliticaMovimientos(gu);

    public static void main(String[] args) {
        cargarDemo();
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║  💳 PLATAFORMA FINTECH - BILLETERAS      ║");
        System.out.println("║     Estructuras de Datos - 2026-1         ║");
        System.out.println("╚══════════════════════════════════════════╝");

        boolean salir = false;
        while (!salir) {
            System.out.println("\n┌──────────────────────────────────────┐");
            System.out.println("│  1. 👤 Usuarios  2. 💼 Billeteras    │");
            System.out.println("│  3. 💸 Transac.  4. 📅 Programadas   │");
            System.out.println("│  5. 🎁 Recomp.   6. 📊 Analítica     │");
            System.out.println("│  7. 🕸️  Grafo    8. 🔔 Notificaciones │");
            System.out.println("│  0. 🚪 Salir                          │");
            System.out.println("└──────────────────────────────────────┘");
            int op = leerInt("Seleccione: ");
            switch (op) {
                case 1 -> menuUsuarios();
                case 2 -> menuBilleteras();
                case 3 -> menuTransacciones();
                case 4 -> menuProgramadas();
                case 5 -> menuRecompensas();
                case 6 -> an.imprimirReporteGeneral();
                case 7 -> menuGrafo();
                case 8 -> menuNotificaciones();
                case 0 -> { salir = true; System.out.println("👋 ¡Hasta luego!"); }
                default -> System.out.println("❌ Opción inválida.");
            }
        }
        sc.close();
    }

    static void menuUsuarios() {
        System.out.println("\n── USUARIOS ──");
        System.out.println("1.Registrar 2.Buscar ID 3.Buscar cédula 4.Listar 5.Actualizar 6.Eliminar");
        int op = leerInt("Opción: ");
        switch (op) {
            case 1 -> gu.registrarUsuario(new Usuario(leerTexto("Nombre: "), leerTexto("Email: "), leerTexto("Cédula: ")));
            case 2 -> { Usuario u = gu.buscarPorId(leerTexto("ID: ")); System.out.println(u != null ? u : "❌ No encontrado"); }
            case 3 -> { Usuario u = gu.buscarPorCedula(leerTexto("Cédula: ")); System.out.println(u != null ? u : "❌ No encontrado"); }
            case 4 -> gu.getTodosUsuarios().forEach(System.out::println);
            case 5 -> gu.actualizarUsuario(leerTexto("ID: "), leerTexto("Nuevo nombre: "), leerTexto("Nuevo email: "));
            case 6 -> gu.eliminarUsuario(leerTexto("ID: "));
        }
    }

    static void menuBilleteras() {
        System.out.println("\n── BILLETERAS ──");
        System.out.println("1.Crear 2.Ver billeteras 3.Desactivar");
        int op = leerInt("Opción: ");
        switch (op) {
            case 1 -> {
                Usuario u = gu.buscarPorId(leerTexto("ID usuario: "));
                if (u == null) { System.out.println("❌ No encontrado"); return; }
                System.out.println("Tipos: 1.AHORRO 2.GASTOS_DIARIOS 3.COMPRAS 4.TRANSPORTE 5.INVERSION");
                int tipo = leerInt("Tipo: ");
                TipoBilletera[] tipos = TipoBilletera.values();
                if (tipo < 1 || tipo > tipos.length) { System.out.println("❌ Tipo inválido"); return; }
                gb.crearBilletera(u, leerTexto("Nombre: "), tipos[tipo-1], leerDouble("Saldo inicial: "));
            }
            case 2 -> { Usuario u = gu.buscarPorId(leerTexto("ID usuario: ")); if (u != null) gb.listarBilleteras(u); }
            case 3 -> gb.desactivarBilletera(leerTexto("ID billetera: "));
        }
    }

    static void menuTransacciones() {
        System.out.println("\n── TRANSACCIONES ──");
        System.out.println("1.Recargar 2.Retirar 3.Transferir 4.Historial 5.Revertir");
        int op = leerInt("Opción: ");
        switch (op) {
            case 1 -> gt.recargar(leerTexto("ID usuario: "), leerTexto("ID billetera: "), leerDouble("Monto: "));
            case 2 -> gt.retirar(leerTexto("ID usuario: "), leerTexto("ID billetera: "), leerDouble("Monto: "));
            case 3 -> gt.transferir(leerTexto("ID usuario origen: "), leerTexto("ID billetera origen: "), leerTexto("ID billetera destino: "), leerDouble("Monto: "));
            case 4 -> {
                Billetera b = gb.buscarPorId(leerTexto("ID billetera: "));
                if (b == null) { System.out.println("❌ No encontrada"); return; }
                System.out.println("\n📋 Historial de " + b.getNombre() + ":");
                b.getHistorialTransacciones().forEach(t -> System.out.println("   " + t));
            }
            case 5 -> gt.revertirUltimaOperacion(leerTexto("ID usuario: "));
        }
    }

    static void menuProgramadas() {
        System.out.println("\n── OPERACIONES PROGRAMADAS ──");
        System.out.println("1.Programar 2.Ejecutar pendientes 3.Ver total");
        int op = leerInt("Opción: ");
        switch (op) {
            case 1 -> {
                String uid = leerTexto("ID usuario: ");
                String bO = leerTexto("ID billetera origen (enter si no aplica): ");
                String bD = leerTexto("ID billetera destino: ");
                double monto = leerDouble("Monto: ");
                String desc = leerTexto("Descripción: ");
                // Fecha en pasado para demostración
                gop.programar(uid, bO.isBlank() ? null : bO, bD,
                        TipoTransaccion.RECARGA, monto, LocalDateTime.now().minusSeconds(1), desc);
            }
            case 2 -> gop.ejecutarOperacionesPendientes();
            case 3 -> System.out.println("📋 Pendientes: " + gop.totalPendientes());
        }
    }

    static void menuRecompensas() {
        System.out.println("\n── RECOMPENSAS ──");
        System.out.println("1.Ver catálogo 2.Canjear");
        int op = leerInt("Opción: ");
        if (op == 1) gr.mostrarCatalogo();
        else if (op == 2) gr.canjearBeneficio(leerTexto("ID usuario: "), leerTexto("ID beneficio: "));
    }

    static void menuGrafo() {
        System.out.println("\n── GRAFO ──");
        System.out.println("1.Ver grafo 2.BFS 3.Detectar ciclos 4.Frecuencias 5.Montos");
        GrafoTransferencias grafo = gt.getGrafo();
        int op = leerInt("Opción: ");
        switch (op) {
            case 1 -> grafo.imprimirGrafo();
            case 2 -> System.out.println("BFS: " + grafo.bfs(leerTexto("ID usuario inicio: ")));
            case 3 -> System.out.println("¿Tiene ciclos? " + (grafo.tieneCiclo() ? "✅ SÍ" : "❌ NO"));
            case 4 -> grafo.getFrecuenciaTransferencias().forEach((k, v) -> System.out.printf("   %s: %d tx%n", k, v));
            case 5 -> grafo.getMontosTotales().forEach((k, v) -> System.out.printf("   %s: $%.2f%n", k, v));
        }
    }

    static void menuNotificaciones() {
        Usuario u = gu.buscarPorId(leerTexto("ID usuario: "));
        if (u == null) { System.out.println("❌ No encontrado"); return; }
        System.out.println("\n🔔 Notificaciones de " + u.getNombre() + ":");
        if (u.getNotificaciones().isEmpty()) System.out.println("   Sin notificaciones.");
        else { int i = 1; for (String n : u.getNotificaciones()) System.out.println("   " + i++ + ". " + n); }
    }

    static void cargarDemo() {
        System.out.println("\n📦 Cargando datos de demostración...");
        Usuario u1 = new Usuario("Ana García", "ana@demo.com", "1001");
        Usuario u2 = new Usuario("Carlos López", "carlos@demo.com", "1002");
        Usuario u3 = new Usuario("María Torres", "maria@demo.com", "1003");
        gu.registrarUsuario(u1); gu.registrarUsuario(u2); gu.registrarUsuario(u3);

        Billetera b1 = gb.crearBilletera(u1, "Ahorro Ana", TipoBilletera.AHORRO, 1000.0);
        Billetera b2 = gb.crearBilletera(u1, "Gastos Diarios", TipoBilletera.GASTOS_DIARIOS, 500.0);
        Billetera b3 = gb.crearBilletera(u2, "Principal Carlos", TipoBilletera.COMPRAS, 2000.0);
        Billetera b4 = gb.crearBilletera(u3, "Transporte", TipoBilletera.TRANSPORTE, 300.0);

        gt.recargar(u1.getId(), b1.getId(), 500.0);
        gt.retirar(u1.getId(), b2.getId(), 100.0);
        gt.transferir(u2.getId(), b3.getId(), b1.getId(), 300.0);
        gt.transferir(u1.getId(), b1.getId(), b4.getId(), 150.0);
        gop.programar(u1.getId(), null, b2.getId(), TipoTransaccion.RECARGA, 100.0,
                LocalDateTime.now().minusMinutes(5), "Recarga automática");

        System.out.println("\n✅ Demo cargada.");
        System.out.println("📌 IDs: Ana=" + u1.getId() + " | Carlos=" + u2.getId() + " | María=" + u3.getId());
    }

    static int leerInt(String m) { System.out.print(m); try { return Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { return -1; } }
    static double leerDouble(String m) { System.out.print(m); try { return Double.parseDouble(sc.nextLine().trim()); } catch (Exception e) { return 0; } }
    static String leerTexto(String m) { System.out.print(m); return sc.nextLine().trim(); }
}