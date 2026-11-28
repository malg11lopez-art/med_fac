# Puertas de calidad — MEDIEVAL FACTIONS v0.9.0-alpha

Estado: alpha en desarrollo. No declarar completa una mecánica solo por tener clase Java, registro SavedData, comando o recurso.

1. **Dominio:** reglas puras deterministas con pruebas de invariantes, límites y casos terminales.
2. **Datos:** serialización NBT real, versión de esquema, migración, round-trip, corrupción y crash/reintento.
3. **Servidor:** economía, daño, movimiento, propiedad, consentimiento, salud y resultado se validan en servidor.
4. **Mundo:** los objetos y cantidades abstractos corresponden a bloques, entidades, inventarios o ubicaciones físicas cuando la mecánica lo requiere.
5. **Cliente:** pantallas, modelos, texturas, animaciones, sonido, tooltips y accesibilidad verificados visualmente.
6. **Persistencia:** guardar, cerrar, reiniciar, descargar chunks y volver a cargar sin duplicados ni pérdidas.
7. **Multijugador:** dos clientes y servidor dedicado; no confiar en datos enviados por cliente.
8. **Rendimiento:** presupuestos de ticks, población alta, rutas, batallas y simulación de regiones distantes.
9. **Compatibilidad:** mundo limpio, mundo preexistente y mods de biomas/estructuras; reload de datapacks.
10. **Distribución:** build reproducible, changelog, versión, manifest, checksum y configuración de servidor.

## Principios no negociables

- Ningún NPC, tienda, banco, clínica o misión crea moneda, materiales, comida, medicina o equipo gratuitamente.
- Los trabajos y convoyes reservan insumos; entregas y pagos son idempotentes.
- Un objeto único tiene identidad y ubicación/propietario trazable.
- El cliente presenta; el servidor decide.
- Una entidad visible mantiene un vínculo persistente único con su entidad de dominio.
- Los sistemas lejanos se abstraen; no se ejecutan miles de entidades activas sin presupuesto.
- El mundo no simula tiempo ilimitado mientras el servidor está apagado.
- Las compatibilidades opcionales no pueden ser requisitos ocultos para que el juego base funcione.

## Estado en v0.9.0-alpha

- Dominio puro: 271 aserciones pasan.
- SavedData: smoke test con stubs pasa, pero solo cubre un subconjunto de SavedData previo.
- Recursos: 106 JSON, 14 rigs, 75 SoundEvents y 75 OGG, sin errores ni advertencias.
- Compilación NeoForge real: no ejecutada; Gradle/Wrapper y dependencias no disponibles.
- Cliente/servidor, GameTests, GUI, persistencia avanzada real y multijugador: pendientes.
