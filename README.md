
## PlayList Maker: Music Search App

## Описание
Приложение для поиска песен с плеером, плейлистами, избранными треками, воспроизведением тестового фрагмента песни. 

## Основные функции

- 🔍 **Поиск песен** - использует музыкальную базу itunes.apple.com
- ❤️ **Избранное** - добавление треков в избранное
- 📁 **Плейлисты** - создание и управление плейлистами
- ▶️ **Плеер** - воспроизведение тестовых фрагментов песен
- 💾 **Сохранение данных** - плейлисты, обложки и настройки сохраняются в Room Database

## Технологии

### Backend & Networking
- **Retrofit 2** - для сетевых запросов к iTunes API
- **Gson** - для парсинга JSON ответов
- **OkHttp3** - HTTP клиент с логированием запросов
- **Coil** - для загрузки и кэширования изображений

### Architecture & DI
- **Kotlin** - основной язык разработки
- **MVVM** - архитектурный паттерн
- **Koin** - dependency injection
- **Single Activity + Fragments** - навигация
- **Jetpack Navigation Component** - управление навигацией
- **Lifecycle & ViewModel** - управление жизненным циклом

### Async & Data
- **Coroutines** - асинхронные операции
- **Flow** - реактивные потоки данных
- **Room Database** - локальное хранилище


### UI
- **XML**
- **Material Design** - современный UI
- **ViewBinding** - привязка view
- **AndroidX** - современные компоненты
- **ViewPager2** - для swipe интерфейсов

## Установка

1. Клонируйте репозиторий
2. Откройте проект в Android Studio
3. Запустите приложение


En: 
Playlist Maker
The application uses the music database from itunes.apple.com.
The app supports English language
Users can retrieve a list of songs based on a query, add songs to favorites, create playlists, add or remove tracks from playlists, and listen to a preview clip. Playlists, album covers, and application settings are stored on the user\'s device using Room database.
Stack: Kotlin, MVVM, Koin, SingleActivity (+Fragments), ViewPager2, Jetpack Navigation Component, Coroutines, Room database.

Скриншоты
<div align="center">
<img src="https://github.com/user-attachments/assets/327bc9b9-8bda-4959-b545-57e7e3b3a944" width="30%" alt="Главный экран поиска">
<img src="https://github.com/user-attachments/assets/f73224cf-8626-4ae7-a7ba-fe893923f6dc" width="30%" alt="Результаты поиска песен">
<img src="https://github.com/user-attachments/assets/0b2c7134-e098-418c-9bcd-f4dc539c7ec3" width="30%" alt="Детали трека и плеер">
<img src="https://github.com/user-attachments/assets/cd3107ce-8c1f-4dd6-84f4-035a75e8ba3d" width="30%" alt="Управление плейлистами">
<img src="https://github.com/user-attachments/assets/e8818408-ae22-4d94-9091-5a260ed30ccb" width="30%" alt="Избранные треки"></div>

