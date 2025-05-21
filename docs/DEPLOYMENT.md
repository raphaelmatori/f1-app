# F1 Champions Explorer Deployment Guide

## Prerequisites

- Docker and Docker Compose installed
- MySQL 8.0+
- Redis 7.0+
- Node.js 20.x
- Java 17
- Nginx (for production)

## Production Deployment Steps

1. **Set up GitHub Secrets**
   ```bash
   chmod +x scripts/setup-github-secrets.sh
   ./scripts/setup-github-secrets.sh
   ```

2. **Configure Production Environment**
   - Copy `.env.production` to `.env`
   - Update all environment variables with production values
   - Ensure all secrets are properly set

3. **Set up Production Database**
   ```bash
   chmod +x scripts/setup-production-db.sh
   ./scripts/setup-production-db.sh
   ```

4. **Build and Deploy Backend**
   ```bash
   cd backend
   ./gradlew build -Pprod
   docker build -t f1-champions-backend:prod .
   docker push f1-champions-backend:prod
   ```

5. **Build and Deploy Frontend**
   ```bash
   cd frontend
   npm run build -- --configuration=production
   docker build -t f1-champions-frontend:prod .
   docker push f1-champions-frontend:prod
   ```

6. **Deploy with Docker Compose**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

## Monitoring Setup

1. **Prometheus Configuration**
   - Install Prometheus
   - Configure to scrape metrics from `/actuator/prometheus`
   - Set up alerting rules

2. **Grafana Dashboard**
   - Install Grafana
   - Import dashboard configuration
   - Set up alerts

3. **Log Management**
   - Configure log rotation
   - Set up log aggregation
   - Configure log retention policies

## Security Checklist

- [ ] SSL/TLS certificates installed
- [ ] Database credentials rotated
- [ ] API keys and secrets updated
- [ ] Firewall rules configured
- [ ] Security headers enabled
- [ ] Rate limiting configured
- [ ] Backup strategy implemented

## Health Checks

The application exposes the following health check endpoints:

- Backend: `https://api.f1-champions-explorer.com/actuator/health`
- Frontend: `https://f1-champions-explorer.com/health`

## Backup and Recovery

1. **Database Backup**
   ```bash
   mysqldump -u root -p f1_champions > backup.sql
   ```

2. **Redis Backup**
   ```bash
   redis-cli SAVE
   ```

3. **Application Backup**
   ```bash
   tar -czf app-backup.tar.gz /var/log/f1-champions/
   ```

## Troubleshooting

Common issues and solutions:

1. **Database Connection Issues**
   - Check database credentials
   - Verify network connectivity
   - Check database logs

2. **Redis Connection Issues**
   - Verify Redis is running
   - Check Redis logs
   - Verify network connectivity

3. **Application Issues**
   - Check application logs
   - Verify environment variables
   - Check resource usage

## Rollback Procedure

1. **Rollback to Previous Version**
   ```bash
   docker-compose -f docker-compose.prod.yml down
   git checkout <previous-version>
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Database Rollback**
   ```bash
   mysql -u root -p f1_champions < backup.sql
   ```

## Maintenance

Regular maintenance tasks:

1. **Weekly**
   - Check logs for errors
   - Monitor resource usage
   - Review security alerts

2. **Monthly**
   - Update dependencies
   - Rotate credentials
   - Review backup strategy

3. **Quarterly**
   - Security audit
   - Performance review
   - Capacity planning 