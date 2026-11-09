# Inactivación y reactivación de registros

La app permite a un administrador del conjunto activo mantener el censo de
propietarios/arrendatarios y las membresías de vigilantes sin eliminar su
historial. Las acciones solicitan confirmación, muestran el resultado y
actualizan el listado.

- En el censo, el filtro `activos` es el predeterminado; `inactivos` muestra
  registros históricos y `todos` combina ambos estados.
- La pantalla de gestión de vigilantes está disponible desde el menú de
  seguridad y usa los mismos filtros.
- Reactivar conserva la identidad del registro o de la membresía original.
- El servidor valida que el usuario sea ADMIN del conjunto de la ruta. Cambiar
  el conjunto activo en la app no sustituye esa autorización.
- El estado de una membresía de vigilante solo afecta la Organization Auth0 de
  ese conjunto; no bloquea la cuenta global ni su acceso a otros conjuntos.

Las mutaciones pasan por API Gateway y no se reintentan automáticamente. Si
Identity Service/Auth0 no está disponible al inactivar un vigilante, el backend
mantiene la membership local inactiva; la acción puede repetirse para completar
la retirada de la Organization. Al reactivar, el backend no habilita la
membership local hasta que Identity Service confirme la asignación del rol.
