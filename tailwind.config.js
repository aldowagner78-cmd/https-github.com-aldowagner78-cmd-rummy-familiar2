/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        felt: {
          dark: '#0F3E26',
          medium: '#145334',
          light: '#1E6C45',
        },
        mahogany: {
          dark: '#2C160B',
          medium: '#422212',
          light: '#5E321C',
        },
        maple: '#9E6B48',
        tile: {
          ivory: '#FDFBF0',
          shadow: '#E9E5CE',
          highlight: '#FFFFFF',
          red: '#D32F2F',
          blue: '#1976D2',
          black: '#212121',
          orange: '#E65100',
          joker: '#6A1B9A',
        },
        gold: '#FFB300',
        bone: '#F5F2E6',
        grey: {
          dark: '#121212',
          card: '#1E1E1E',
        }
      },
    },
  },
  plugins: [],
}
