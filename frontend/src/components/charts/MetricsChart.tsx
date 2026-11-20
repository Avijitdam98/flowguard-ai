import React from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Legend,
} from 'recharts';

export interface MetricsChartProps {
  type: 'area' | 'line' | 'bar' | 'pie';
  data: any[];
  height?: number;
  dataKey?: string;
  secondaryDataKey?: string;
  name?: string;
  secondaryName?: string;
  color?: string;
  secondaryColor?: string;
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-navy-950 border border-navy-700 p-2.5 rounded-lg shadow-xl text-xs">
        <p className="font-semibold text-slate-300 mb-1">{label}</p>
        {payload.map((entry: any, index: number) => (
          <div key={`item-${index}`} className="flex items-center gap-2 py-0.5">
            <span
              className="w-2 h-2 rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="text-slate-400">{entry.name}:</span>
            <span className="font-mono font-medium text-slate-100">
              {typeof entry.value === 'number' ? entry.value.toLocaleString() : entry.value}
            </span>
          </div>
        ))}
      </div>
    );
  }
  return null;
};

export const MetricsChart: React.FC<MetricsChartProps> = ({
  type,
  data,
  height = 260,
  dataKey = 'value',
  secondaryDataKey,
  name = 'Value',
  secondaryName = 'Secondary',
  color = '#3B82F6',
  secondaryColor = '#EF4444',
}) => {
  const PIE_COLORS = ['#10B981', '#F59E0B', '#EF4444', '#64748B'];

  if (type === 'area') {
    return (
      <ResponsiveContainer width="100%" height={height}>
        <AreaChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <defs>
            <linearGradient id="primaryGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={color} stopOpacity={0.4} />
              <stop offset="95%" stopColor={color} stopOpacity={0.0} />
            </linearGradient>
            {secondaryDataKey && (
              <linearGradient id="secondaryGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={secondaryColor} stopOpacity={0.4} />
                <stop offset="95%" stopColor={secondaryColor} stopOpacity={0.0} />
              </linearGradient>
            )}
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" vertical={false} />
          <XAxis dataKey="timestamp" stroke="#64748B" fontSize={11} tickLine={false} />
          <YAxis stroke="#64748B" fontSize={11} tickLine={false} />
          <Tooltip content={<CustomTooltip />} />
          <Area
            type="monotone"
            dataKey={dataKey}
            name={name}
            stroke={color}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#primaryGradient)"
          />
          {secondaryDataKey && (
            <Area
              type="monotone"
              dataKey={secondaryDataKey}
              name={secondaryName}
              stroke={secondaryColor}
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#secondaryGradient)"
            />
          )}
        </AreaChart>
      </ResponsiveContainer>
    );
  }

  if (type === 'line') {
    return (
      <ResponsiveContainer width="100%" height={height}>
        <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" vertical={false} />
          <XAxis dataKey="timestamp" stroke="#64748B" fontSize={11} tickLine={false} />
          <YAxis stroke="#64748B" fontSize={11} tickLine={false} />
          <Tooltip content={<CustomTooltip />} />
          <Line
            type="monotone"
            dataKey={dataKey}
            name={name}
            stroke={color}
            strokeWidth={2}
            dot={false}
          />
          {secondaryDataKey && (
            <Line
              type="monotone"
              dataKey={secondaryDataKey}
              name={secondaryName}
              stroke={secondaryColor}
              strokeWidth={2}
              strokeDasharray="4 4"
              dot={false}
            />
          )}
        </LineChart>
      </ResponsiveContainer>
    );
  }

  if (type === 'bar') {
    return (
      <ResponsiveContainer width="100%" height={height}>
        <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" vertical={false} />
          <XAxis dataKey="timestamp" stroke="#64748B" fontSize={11} tickLine={false} />
          <YAxis stroke="#64748B" fontSize={11} tickLine={false} />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey={dataKey} name={name} fill={color} radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    );
  }

  if (type === 'pie') {
    return (
      <ResponsiveContainer width="100%" height={height}>
        <PieChart>
          <Tooltip content={<CustomTooltip />} />
          <Legend
            verticalAlign="bottom"
            height={36}
            formatter={(val) => <span className="text-xs text-slate-300">{val}</span>}
          />
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="45%"
            innerRadius={45}
            outerRadius={75}
            paddingAngle={4}
          >
            {data.map((_, index) => (
              <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
            ))}
          </Pie>
        </PieChart>
      </ResponsiveContainer>
    );
  }

  return null;
};

