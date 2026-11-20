import React, { ReactNode, useState, useMemo } from 'react';
import { ChevronLeft, ChevronRight, Search } from 'lucide-react';
import { LoadingSkeleton } from './LoadingSkeleton';
import { EmptyState } from './EmptyState';

export interface Column<T> {
  key: string;
  header: string;
  render?: (item: T) => ReactNode;
  width?: string;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  searchable?: boolean;
  searchPlaceholder?: string;
  searchFilter?: (item: T, query: string) => boolean;
  emptyTitle?: string;
  emptyDescription?: string;
  itemsPerPage?: number;
}

export function DataTable<T extends Record<string, unknown>>({
  columns,
  data,
  isLoading = false,
  searchable = false,
  searchPlaceholder = 'Search records...',
  searchFilter,
  emptyTitle = 'No data found',
  emptyDescription = 'There are no records matching your criteria.',
  itemsPerPage = 10,
}: DataTableProps<T>) {
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);

  const filteredData = useMemo(() => {
    if (!searchQuery || !searchFilter) return data;
    return data.filter((item) => searchFilter(item, searchQuery.toLowerCase()));
  }, [data, searchQuery, searchFilter]);

  const totalPages = Math.ceil(filteredData.length / itemsPerPage) || 1;
  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return filteredData.slice(start, start + itemsPerPage);
  }, [filteredData, currentPage, itemsPerPage]);

  return (
    <div className="w-full">
      {searchable && (
        <div className="mb-4 flex items-center justify-between">
          <div className="relative w-72">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setCurrentPage(1);
              }}
              placeholder={searchPlaceholder}
              className="w-full bg-navy-950 border border-navy-700 rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-electric-500"
            />
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-lg border border-navy-750">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-navy-950/70 border-b border-navy-750 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              {columns.map((col) => (
                <th key={col.key} className={`px-4 py-3 ${col.width || ''}`}>
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-navy-750/50 bg-navy-900/60 text-xs text-slate-200">
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>
                  <td colSpan={columns.length} className="px-4 py-3">
                    <LoadingSkeleton height="h-6" />
                  </td>
                </tr>
              ))
            ) : paginatedData.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="p-8">
                  <EmptyState title={emptyTitle} description={emptyDescription} />
                </td>
              </tr>
            ) : (
              paginatedData.map((row, idx) => (
                <tr
                  key={idx}
                  className="hover:bg-navy-800/50 transition-colors"
                >
                  {columns.map((col) => (
                    <td key={col.key} className="px-4 py-3">
                      {col.render ? col.render(row) : (row[col.key] as ReactNode)}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {!isLoading && filteredData.length > itemsPerPage && (
        <div className="flex items-center justify-between px-2 py-3 text-xs text-slate-400">
          <div>
            Showing {(currentPage - 1) * itemsPerPage + 1} to{' '}
            {Math.min(currentPage * itemsPerPage, filteredData.length)} of {filteredData.length} records
          </div>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="p-1 rounded bg-navy-800 border border-navy-700 disabled:opacity-40 hover:bg-navy-700"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="px-2">
              Page {currentPage} of {totalPages}
            </span>
            <button
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
              className="p-1 rounded bg-navy-800 border border-navy-700 disabled:opacity-40 hover:bg-navy-700"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

