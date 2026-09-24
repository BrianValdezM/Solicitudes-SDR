/**
 * Medidor de fortaleza de contraseña.
 * Uso en HTML:
 *   <input type="password" id="pwd" data-pwd-input>
 *   <div data-pwd-meter></div>
 *   <div data-pwd-label></div>
 *
 * Reglas evaluadas:
 *  - Mínimo 8 caracteres
 *  - Al menos una minúscula
 *  - Al menos una mayúscula
 *  - Al menos un número
 *  - Al menos un símbolo
 */
(function () {
    'use strict';

    const REGLAS = [
        { id: 'len',     etiqueta: 'Mínimo 8 caracteres', test: v => v.length >= 8 },
        { id: 'minus',   etiqueta: 'Una minúscula',       test: v => /[a-z]/.test(v) },
        { id: 'mayus',   etiqueta: 'Una mayúscula',       test: v => /[A-Z]/.test(v) },
        { id: 'num',     etiqueta: 'Un número',           test: v => /\d/.test(v) },
        { id: 'sim',     etiqueta: 'Un símbolo',          test: v => /[^A-Za-z0-9]/.test(v) }
    ];

    const NIVELES = {
        1: 'Muy débil',
        2: 'Débil',
        3: 'Buena',
        4: 'Fuerte'
    };

    function construirMeter(input) {
        // Encontrar los contenedores relacionados (por orden en el DOM)
        const meterDiv = input.parentElement.querySelector('[data-pwd-meter]');
        const labelDiv = input.parentElement.querySelector('[data-pwd-label]');
        if (!meterDiv) return;

        // Construir barras + checklist si no existen
        meterDiv.classList.add('pwd-meter');
        if (!meterDiv.querySelector('.pwd-barras')) {
            meterDiv.innerHTML = `
                <div class="pwd-barras">
                    <div class="pwd-barra"></div>
                    <div class="pwd-barra"></div>
                    <div class="pwd-barra"></div>
                    <div class="pwd-barra"></div>
                </div>
                <ul class="pwd-checklist">
                    ${REGLAS.map(r => `<li data-regla="${r.id}">${r.etiqueta}</li>`).join('')}
                </ul>
            `;
        }

        const items = meterDiv.querySelectorAll('.pwd-checklist li');

        function evaluar() {
            const v = input.value || '';
            let cumple = 0;

            REGLAS.forEach((regla, i) => {
                const ok = v.length > 0 && regla.test(v);
                items[i].classList.toggle('ok', ok);
                if (ok) cumple++;
            });

            // Nivel: cuántas reglas se cumplen de las 5
            // 0-1 => 1 (muy débil), 2 => 2 (débil), 3-4 => 3 (buena), 5 => 4 (fuerte)
            let nivel = 0;
            if (v.length === 0) nivel = 0;
            else if (cumple <= 1) nivel = 1;
            else if (cumple === 2) nivel = 2;
            else if (cumple <= 4) nivel = 3;
            else nivel = 4;

            if (nivel > 0) {
                meterDiv.setAttribute('data-nivel', nivel);
            } else {
                meterDiv.removeAttribute('data-nivel');
            }

            if (labelDiv) {
                labelDiv.textContent = nivel > 0 ? NIVELES[nivel] : '';
            }
        }

        input.addEventListener('input', evaluar);
        input.addEventListener('change', evaluar);
        evaluar();
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('input[data-pwd-input]').forEach(construirMeter);
    });
})();