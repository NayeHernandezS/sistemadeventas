package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.util.LogoImageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class TenantLogoService {

    private static final long MAX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp");

    private final PreferenciasTenantService preferenciasTenantService;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    public TenantLogoService(PreferenciasTenantService preferenciasTenantService) {
        this.preferenciasTenantService = preferenciasTenantService;
    }

    public boolean tieneLogo(String tenant) {
        if (!tenantValido(tenant)) {
            return false;
        }
        return preferenciasTenantService.tieneLogo(tenant) && rutaLogoEnDisco(tenant).isPresent();
    }

    public String urlLogo(String contextPath) {
        return contextPath + "/tenant/logo";
    }

    public void guardarLogo(String tenant, MultipartFile archivo) {
        validarTenant(tenant);
        if (archivo == null || archivo.isEmpty()) {
            throw new ServiceJdbcException("Selecciona una imagen para el logo", null);
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ServiceJdbcException("El logo no puede superar 2 MB", null);
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ServiceJdbcException("Formato no valido. Usa PNG, JPG o WebP", null);
        }
        String extension = extensionPara(contentType);
        String filename = "logo." + extension;
        Path directorio = directorioTenant(tenant);
        try {
            Files.createDirectories(directorio);
            borrarLogosAnteriores(directorio);
            Path destino = directorio.resolve(filename);
            if (LogoImageUtil.puedeNormalizar(contentType)) {
                try (InputStream entrada = archivo.getInputStream()) {
                    LogoImageUtil.normalizarYGuardar(entrada, destino, extension);
                }
            } else {
                archivo.transferTo(destino.toFile());
            }
            preferenciasTenantService.guardarLogoFilename(tenant, filename);
        } catch (IOException e) {
            throw new ServiceJdbcException("No se pudo guardar el logo: " + e.getMessage(), e);
        }
    }

    public void eliminarLogo(String tenant) {
        validarTenant(tenant);
        Path directorio = directorioTenant(tenant);
        try {
            if (Files.isDirectory(directorio)) {
                borrarLogosAnteriores(directorio);
            }
            preferenciasTenantService.eliminarLogo(tenant);
        } catch (IOException e) {
            throw new ServiceJdbcException("No se pudo eliminar el logo: " + e.getMessage(), e);
        }
    }

    public Optional<LogoRecurso> obtenerLogo(String tenant) {
        if (!tenantValido(tenant) || !preferenciasTenantService.tieneLogo(tenant)) {
            return Optional.empty();
        }
        return rutaLogoEnDisco(tenant).flatMap(path -> {
            if (!Files.isRegularFile(path)) {
                return Optional.empty();
            }
            String contentType = contentTypePorArchivo(path);
            return Optional.of(new LogoRecurso(new FileSystemResource(path), contentType));
        });
    }

    private Optional<Path> rutaLogoEnDisco(String tenant) {
        return preferenciasTenantService.consultar(tenant)
                .map(p -> p.getLogoFilename())
                .filter(f -> f != null && !f.isBlank())
                .map(filename -> directorioTenant(tenant).resolve(filename))
                .filter(path -> {
                    try {
                        return Files.isRegularFile(path);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    private Path directorioTenant(String tenant) {
        Path base = Path.of(uploadsDir, "tenants").normalize().toAbsolutePath();
        Path destino = base.resolve(tenant.trim()).normalize();
        if (!destino.startsWith(base)) {
            throw new ServiceJdbcException("Ruta de logo no valida", null);
        }
        return destino;
    }

    private static void borrarLogosAnteriores(Path directorio) throws IOException {
        if (!Files.isDirectory(directorio)) {
            return;
        }
        try (Stream<Path> archivos = Files.list(directorio)) {
            archivos.filter(p -> p.getFileName().toString().startsWith("logo."))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                            // continuar con otros archivos
                        }
                    });
        }
    }

    private static String extensionPara(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private static String contentTypePorArchivo(Path path) {
        String nombre = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".png")) {
            return "image/png";
        }
        if (nombre.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private static void validarTenant(String tenant) {
        if (!tenantValido(tenant)) {
            throw new ServiceJdbcException("Cuenta de negocio no valida", null);
        }
    }

    private static boolean tenantValido(String tenant) {
        return tenant != null && !tenant.isBlank() && tenant.matches("[A-Za-z0-9_\\-]+");
    }

    public record LogoRecurso(Resource resource, String contentType) {
    }
}
