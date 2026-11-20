import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  Network,
  ExternalLink,
  Server,
  Plus,
  RefreshCw,
  Search,
  Activity,
  Layers,
  Trash2,
} from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { StatusBadge } from '../../components/ui/StatusBadge';
import { LoadingSkeleton } from '../../components/ui/LoadingSkeleton';
import { EmptyState } from '../../components/ui/EmptyState';
import { Modal } from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { serviceRegistryService } from '../../services/serviceRegistryService';
import { CreateServiceRequest, ServiceItem } from '../../types/service';
import { MonitorStatus } from '../../types/monitor';

export const ServicesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const toast = useToast();

  const [search, setSearch] = useState('');
  const [selectedEnv, setSelectedEnv] = useState('ALL');
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
  const [formData, setFormData] = useState<CreateServiceRequest>({
    name: '',
    slug: '',
    description: '',
    environment: 'PRODUCTION',
    currentStatus: 'HEALTHY',
  });

  const {
    data: servicesData,
    isLoading,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ['servicesRegistry', selectedEnv],
    queryFn: () =>
      serviceRegistryService.getAllServices({
        environment: selectedEnv !== 'ALL' ? selectedEnv : undefined,
        size: 50,
      }),
  });

  const registerMutation = useMutation({
    mutationFn: (req: CreateServiceRequest) => serviceRegistryService.registerService(req),
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ['servicesRegistry'] });
      toast.success('Service Registered', `${created.name} added to architecture catalog`);
      setIsRegisterModalOpen(false);
      setFormData({
        name: '',
        slug: '',
        description: '',
        environment: 'PRODUCTION',
        currentStatus: 'HEALTHY',
      });
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to register service';
      toast.error('Error', msg);
    },
  });

  const handleRegisterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim() || !formData.slug.trim()) {
      toast.warning('Required Fields', 'Name and Slug are required');
      return;
    }
    registerMutation.mutate(formData);
  };

  const services = servicesData?.content || [];
  const filtered = services.filter((s) =>
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.slug.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            <Network className="w-5 h-5 text-electric-400" /> Microservice Registry &amp; Topology
            {isFetching && <RefreshCw className="w-3.5 h-3.5 text-slate-400 animate-spin" />}
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Registered distributed services, architectural health heartbeats, and cluster dependencies
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsRegisterModalOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Register Service
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <Card className="p-3">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative flex-1 w-full">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search services by name or slug..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg pl-9 pr-3 py-2 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
            />
          </div>

          <select
            value={selectedEnv}
            onChange={(e) => setSelectedEnv(e.target.value)}
            className="bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-2.5 py-2 text-slate-300 focus:outline-none focus:border-electric-500"
          >
            <option value="ALL">All Environments</option>
            <option value="PRODUCTION">Production</option>
            <option value="STAGING">Staging</option>
            <option value="DEVELOPMENT">Development</option>
          </select>
        </div>
      </Card>

      {/* Services Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          <LoadingSkeleton className="h-44" />
          <LoadingSkeleton className="h-44" />
          <LoadingSkeleton className="h-44" />
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState
          title="No Services Found"
          description="Register your microservices to trace dependencies and health."
          action={
            <Button variant="primary" size="sm" onClick={() => setIsRegisterModalOpen(true)}>
              Register Service
            </Button>
          }
          icon={<Network className="w-8 h-8 text-slate-500" />}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map((svc) => (
            <Card key={svc.id} className="hover:border-navy-600 transition-all flex flex-col justify-between">
              <div>
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-2.5">
                    <div className="w-9 h-9 rounded-lg bg-navy-800 border border-navy-700 flex items-center justify-center text-electric-400">
                      <Server className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-sm font-bold text-white">{svc.name}</h3>
                      <p className="text-[10px] text-slate-400 font-mono">
                        {svc.slug} • {svc.environment}
                      </p>
                    </div>
                  </div>
                  <StatusBadge status={svc.currentStatus} size="sm" pulse={svc.currentStatus === 'DOWN'} />
                </div>

                <p className="text-xs text-slate-300 mt-3 line-clamp-2">
                  {svc.description || 'No description provided.'}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-navy-750 flex items-center justify-between text-xs">
                <span className="text-[11px] text-slate-400">
                  Added {new Date(svc.createdAt).toLocaleDateString()}
                </span>
                <Link to={`/services/${svc.id}`}>
                  <Button variant="ghost" size="sm" rightIcon={<ExternalLink className="w-3 h-3" />}>
                    Details
                  </Button>
                </Link>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Register Service Modal */}
      <Modal
        isOpen={isRegisterModalOpen}
        onClose={() => setIsRegisterModalOpen(false)}
        title="Register Microservice"
        subtitle="Catalog a new distributed service component in FlowGuard AI"
        maxWidth="md"
      >
        <form onSubmit={handleRegisterSubmit} className="space-y-4">
          <Input
            label="Service Name *"
            placeholder="e.g. Payment Service"
            value={formData.name}
            onChange={(e) => {
              const name = e.target.value;
              const slug = name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
              setFormData({ ...formData, name, slug });
            }}
            required
          />

          <Input
            label="Service Slug *"
            placeholder="e.g. payment-service"
            value={formData.slug}
            onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
            required
          />

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Environment</label>
            <select
              value={formData.environment}
              onChange={(e) => setFormData({ ...formData, environment: e.target.value })}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-electric-500"
            >
              <option value="PRODUCTION">Production</option>
              <option value="STAGING">Staging</option>
              <option value="DEVELOPMENT">Development</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">Description</label>
            <textarea
              rows={3}
              placeholder="Primary responsibilities, datastore dependencies, and SLAs..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full bg-navy-950/60 border border-navy-750 text-xs rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-electric-500"
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-navy-750">
            <Button variant="ghost" type="button" onClick={() => setIsRegisterModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" isLoading={registerMutation.isPending}>
              Register
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
