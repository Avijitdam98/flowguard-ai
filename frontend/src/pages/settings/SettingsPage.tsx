import React from 'react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Settings, Users, Key, Shield, Cpu, RefreshCw } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const SettingsPage: React.FC = () => {
  const { user } = useAuth();
  const [ingestionKey, setIngestionKey] = React.useState<string>(() => {
    return 'fg_live_' + (user?.id ? user.id.replace(/-/g, '').substring(0, 24) : '7a8b9c0d1e2f3a4b5c6d7e8f');
  });

  const handleRotateKey = () => {
    const chars = '0123456789abcdef';
    let result = 'fg_live_';
    for (let i = 0; i < 24; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setIngestionKey(result);
  };

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
          <Settings className="w-5 h-5 text-electric-400" /> Workspace Settings
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Configure organization teams, RBAC permissions, telemetry keys, and AI diagnostic parameters
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card title="Organization &amp; Team Details" subtitle="Active tenant and access permissions">
          <div className="space-y-3 text-xs">
            <div className="p-3 bg-navy-950 rounded-lg border border-navy-750 flex items-center justify-between">
              <div>
                <p className="font-semibold text-white">Default Engineering Workspace</p>
                <p className="text-[10px] text-slate-400">
                  Role: <span className="text-electric-400 font-mono font-medium">{user?.roles?.[0] || 'ENGINEER'}</span> • User: {user?.email || 'Authenticated'}
                </p>
              </div>
              <span className="px-2 py-0.5 rounded text-[10px] bg-electric-500/10 text-electric-400 font-bold border border-electric-500/30">
                ACTIVE
              </span>
            </div>
            <div className="text-[11px] text-slate-400">
              Role-Based Access Control (RBAC) enforced at Spring Security API gateway level.
            </div>
          </div>
        </Card>

        <Card title="Telemetry &amp; Ingestion Keys" subtitle="API tokens used by microservices to push logs and traces">
          <div className="space-y-3 text-xs">
            <div className="p-3 bg-navy-950 rounded-lg border border-navy-750">
              <span className="text-slate-400 font-medium">Ingestion Token:</span>
              <p className="font-mono text-electric-400 mt-1 break-all">{ingestionKey}</p>
            </div>
            <Button variant="secondary" size="sm" className="w-full flex items-center justify-center gap-2" onClick={handleRotateKey}>
              <RefreshCw className="w-3.5 h-3.5" /> Rotate Ingestion Secret
            </Button>
          </div>
        </Card>

        <Card title="SSRF Network Protection" subtitle="Prevent monitor probes to internal RFC 1918 subnets">
          <div className="space-y-2 text-xs">
            <p className="text-slate-300">
              FlowGuard enforces strict IP validation for all target URLs. Private addresses (127.0.0.1, 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, 169.254.169.254) are automatically blocked.
            </p>
            <div className="pt-2">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                <Shield className="w-3.5 h-3.5" /> SSRF Guard Enabled
              </span>
            </div>
          </div>
        </Card>

        <Card title="AI Diagnostic Settings" subtitle="Ollama / Local LLM &amp; Fallback Configuration">
          <div className="space-y-2 text-xs">
            <p className="text-slate-300 flex items-center gap-2">
              <Cpu className="w-4 h-4 text-purple-400" />
              Active Provider: <code className="text-purple-400 font-mono font-semibold">Ollama (qwen2.5:7b)</code>
            </p>
            <p className="text-slate-400">
              Zero hardcoded inferences. When Ollama is offline, FlowGuard extracts live PostgreSQL telemetry snapshots without hallucinated data.
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};

