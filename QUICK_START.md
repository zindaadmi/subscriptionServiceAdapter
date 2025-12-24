# ⚡ Quick Start

## Start the Application

### Method 1: Using Gradle (Easiest)
```bash
./gradlew run
```

### Method 2: Using Startup Script
```bash
./run.sh
```

### Method 3: Build and Run JAR
```bash
./gradlew jar
java -jar build/libs/subscription-service-1.0.0-SNAPSHOT.jar
```

## Verify It's Running

Open your browser or use curl:
```bash
curl http://localhost:8080/health
```

You should see:
```json
{"status":"UP","service":"subscription-service"}
```

## Configuration

Edit `src/main/resources/application.yml` to change:
- Server port (default: 8080)
- Database settings (default: H2 in-memory)
- JWT settings
- Service configurations

## Stop the Application

Press `Ctrl+C` in the terminal where it's running.

## Troubleshooting

**Port already in use?**
- Change port in `application.yml`: `server.port: 8081`

**Class not found?**
- Run: `./gradlew clean build`

**Database connection failed?**
- Check database credentials in `application.yml`
- For H2 (default), no setup needed
- For MySQL/PostgreSQL, ensure database is running

## Next Steps

1. Test health endpoint: `GET http://localhost:8080/health`
2. Register user: `POST http://localhost:8080/api/auth/register`
3. Check logs in console or `logs/application.log`

For detailed information, see [STARTUP_GUIDE.md](STARTUP_GUIDE.md)

