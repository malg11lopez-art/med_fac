# MEDIEVAL FACTIONS — v0.6.0-alpha

## Novedades
- Moneda física en tres denominaciones: cobre (1), plata (10) y oro (100).
- Depósitos bancarios conectados al inventario: las monedas se consumen y el saldo del cliente y la reserva de la sucursal aumentan de forma atómica.
- Retiradas conectadas al inventario: se comprueba primero el espacio, se reduce el saldo y la reserva, y se entregan las monedas exactas.
- Transferencias bancarias limitadas a cuentas de la misma sucursal; se evita mover pasivos entre reservas sin compensación interbancaria.
- Los préstamos se desembolsan como monedas físicas y descuentan la reserva al concederse; no se duplica el principal en el saldo de cuenta. Para amortizar, el jugador deposita efectivo y el pago reduce su saldo y la deuda sin sumar otra vez la reserva.

## Correcciones y validación
- Corregido el sentido económico de depósitos y retiradas.
- Añadidas pruebas de desglose de monedas, pago exacto, reservas, cuentas congeladas y desbordamientos.
- Pruebas de dominio: 134 aserciones del núcleo + 59 avanzadas = 193 PASS.
- Persistencia NBT con stubs: PASS.
- Validación de recursos: 0 errores y 0 advertencias.

## Límites conocidos
- No hay cambio automático; el jugador debe disponer de denominaciones exactas.
- No hay transferencias entre sucursales hasta implementar compensación interbancaria.
- Esta versión no completa las mecánicas pendientes del Documento Maestro.
- No se ha ejecutado una compilación real NeoForge ni una sesión dentro de Minecraft; no hay JAR certificado.
