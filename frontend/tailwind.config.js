/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Bảng màu nền SOC dashboard: nền tối, chữ tương phản cao (đặc tả mục 27)
        surface: {
          base: '#0b0f14',
          panel: '#121821',
          border: '#1f2937',
        },
        severity: {
          low: '#3b82f6',
          medium: '#eab308',
          high: '#f97316',
          critical: '#ef4444',
        },
      },
    },
  },
  plugins: [],
};