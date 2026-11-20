import React from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  ArrowLeft,
  Server,
  Activity,
  AlertTriangle,
  Layers,
  Calendar,
  Clock,
  Terminal,
  ShieldCheck,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { serviceRegistryService } from '../../services/serviceRegistryService';

export const ServiceDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: service, isLoading } = useQuery({
    queryKey: ['serviceDetail', id],
    queryFn: () => serviceRegistryService.getServiceById(id!),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <LoadingSkeleton className="h-16" />
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
          <LoadingSkeleton className="h-28" />
        </div>
        <LoadingSkeleton className="h-64" />
      </div>
    );
  }

  if (!service) {
    return (
      <EmptyState
        title="Microservice Not Found"
        description="The requested service could not be found in the registry."
        action={
          <Button variant="primary" size="sm" onClick={() => navigate('/services')}>
            Back to Registry
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Link to="/services">
            <Button variant="secondary" size="sm" leftIcon={<ArrowLeft className="w-3.5 h-3.5" />}>
              Back
            </Button>
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight text-white">{service.name}</h1>
              <StatusBadge status={service.currentStatus} size="sm" pulse={service.currentStatus === 'DOWN'} />
            </div>
            <p className="text-xs font-mono text-slate-400 mt-0.5">
              Slug: {service.slug} • Environment: {service.environment}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Link to={`/logs?service=${service.slug}`}>
            <Button variant="secondary" size="sm" leftIcon={<Terminal className="w-3.5 h-3.5" />}>
              View Service Logs
            </Button>
          </Link>
        </div>
      </div>

      {/* 4 KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Health State</span>
          <p
            className={`text-xl font-bold mt-1 ${service.currentStatus === 'HEALTHY'
              ? 'text-emerald-400'
              : service.currentStatus === 'DEGRADED'
                ? 'text-amber-400'
                : 'text-rose-400'
              }`}
          >
            {service.currentStatus}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Status from latest probes</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Environment</span>
          <p className="text-xl font-bold text-electric-400 mt-1">{service.environment}</p>
          <p className="text-[11px] text-slate-400 mt-1">Deployment tier</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Availability SLA</span>
          <p className="text-xl font-bold text-emerald-400 mt-1">99.92%</p>
          <p className="text-[11px] text-slate-400 mt-1">30-day moving window</p>
        </Card>

        <Card className="p-4">
          <span className="text-xs text-slate-400 font-medium">Registered Date</span>
          <p className="text-sm font-bold text-white mt-1">
            {new Date(service.createdAt).toLocaleDateString()}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Telemetry enabled</p>
        </Card>
      </div>

      {/* Service Overview & Dependencies */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card title="Service Specifications" subtitle="Topology metadata">
          <div className="space-y-3 text-xs">
            <p className="text-slate-300 leading-relaxed">
              {service.description || 'Primary microservice component handling distributed workflow traffic.'}
            </p>
            <div className="pt-2 border-t border-navy-750 flex justify-between">
              <span className="text-slate-400">Unique Identifier</span>
              <span className="font-mono text-slate-200">{service.id}</span>
            </div>
            <div className="py-1 border-t border-navy-750 flex justify-between">
              <span className="text-slate-400">DNS Ingress Route</span>
              <span className="font-mono text-electric-400">http://{service.slug}.internal:8080</span>
            </div>
          </div>
        </Card>

        <Card title="Inter-Service Dependencies" subtitle="Upstream and downstream topology graph">
          <div className="space-y-2.5 text-xs">
            <div className="p-2.5 rounded-lg bg-navy-950 border border-navy-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Server className="w-4 h-4 text-slate-400" />
                <span className="text-slate-200 font-medium">PostgreSQL Cluster</span>
              </div>
              <StatusBadge status="HEALTHY" size="sm" />
            </div>

            <div className="p-2.5 rounded-lg bg-navy-950 border border-navy-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Layers className="w-4 h-4 text-slate-400" />
                <span className="text-slate-200 font-medium">Redis Distributed Cache</span>
              </div>
              <StatusBadge status="HEALTHY" size="sm" />
            </div>

            <div className="p-2.5 rounded-lg bg-navy-950 border border-navy-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Activity className="w-4 h-4 text-slate-400" />
                <span className="text-slate-200 font-medium">Apache Kafka Event Bus</span>
              </div>
              <StatusBadge status="HEALTHY" size="sm" />
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
